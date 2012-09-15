#include "cpu.cpp"
#include "headers.cpp"

class Client {

private:

	int sock;
	ip_port server_port;
	ip_address server_addr;

public:

	Client(const char *server_ip, ip_port server_port) {
		memset(&this->server_addr, 0, sizeof(this->server_addr));

		this->server_addr.sin_family = AF_INET;
		this->server_addr.sin_addr.s_addr = inet_addr(server_ip);
		this->server_addr.sin_port = htons(server_port);
	}

	int start() {
		this->sock = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP);

		if(this->sock < 0) {
			DieWithError("socket() failed");
			return 0;
		}

		if(connect(this->sock, (address*)&this->server_addr, sizeof(this->server_addr)) < 0) {
			DieWithError("connect() failed");
			return 0;
		}
		
		return 1;
	}

	void end() {
		if(close(this->sock) < 0) {
			DieWithError("close() failed");
		}
	}

	int send_stat_old() {
		char buffer[MAX_BUFFER_SIZE];

		system("ruby usage.rb");

		FILE *fp = fopen("stat.bin", "r");
		if(fp) {			
			int bytes_sent = 0;		
			int bytes = fread(buffer, 1, MAX_BUFFER_SIZE, fp);
			while(bytes) {
				int tmp = send(this->sock, buffer, bytes, 0);
				if(tmp != bytes)
					DieWithError("send() failed");
				bytes_sent += bytes;
				bytes = fread(buffer, 1, MAX_BUFFER_SIZE, fp);
			}			
			fclose(fp);

			return bytes_sent;
		}
		else {
			DieWithError("send() failed");
			return -1;
		}
	}

	int send_stat() {
		Statistics stat;
		stat.collect();
		
		int size = sizeof(data);
		int bytes_sent = send(this->sock, (void*)(&stat.d), size, 0);
		if(bytes_sent != size)
			DieWithError("send() failed");

		return bytes_sent;
	}
};


int main() {
	Client c("127.0.0.1", 5000);

	while(1) {	
		int rs = c.start();
		if(rs) {
			int bytes_sent = c.send_stat();
			cout << bytes_sent << " bytes sent" << endl;
			c.end();
		}
		sleep(10);
	}
}
