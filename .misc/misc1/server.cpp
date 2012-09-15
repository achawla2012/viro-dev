#include "headers.cpp"

typedef struct {
	int sock;
	ip_address addr;
	socklen_t addr_len;
} client_info;

class Server {

private:

	int sock;
	ip_port server_port;
	ip_address server_addr;

	static const int MAX_PENDING = 20;

public:

	Server(const char *server_ip, ip_port server_port) {
		memset(&this->server_addr, 0, sizeof(this->server_addr));

		this->server_addr.sin_family = AF_INET;
		if(server_ip == NULL)
			this->server_addr.sin_addr.s_addr = htonl(INADDR_ANY);
		else
			this->server_addr.sin_addr.s_addr = inet_addr(server_ip);

		this->server_addr.sin_port = htons(server_port);
	}

	void start() {
		this->sock = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP);

		if(this->sock < 0) {
			DieWithError("socket() failed");
			return;
		}

		if(bind(this->sock, (address*)&this->server_addr, sizeof(this->server_addr)) < 0) {
			DieWithError("bind() failed");
		}

		if(listen(this->sock, MAX_PENDING) < 0) {
			DieWithError("listen() failed");
		}
	}

	client_info client() {
		client_info info;
		info.sock = accept(this->sock, (address*)&info.addr, &info.addr_len);
		if(info.sock < 0) {
			DieWithError("accept() failed");
		}

		return info;
	}

	void end() {
		if(close(this->sock) < 0) {
			DieWithError("close() failed");
		}
	}
};

class ClientHandler {
private:
	client_info cinfo;	

public:
	ClientHandler(client_info cinfo) {
		this->cinfo = cinfo;
	}

	static void* handle(void *c) {
		ClientHandler *client = (ClientHandler*)c;

		char buffer[MAX_BUFFER_SIZE];

		char fname[100];
		sprintf(fname, "../stat/machine-%s-%ld.bin", inet_ntoa(client->cinfo.addr.sin_addr), time(NULL));		
		
		FILE *fp = fopen(fname, "w");
		if(!fp) {
			DieWithError("cant open file", __FILE__, __LINE__);
			return NULL;
		}

		cout << "receiveing data ...\n";

		int recv_bytes = recv(client->cinfo.sock, buffer, MAX_BUFFER_SIZE, 0);
		if(recv_bytes < 0) {
			DieWithError("recv() failed");
		}

		while(recv_bytes > 0) {
			fwrite(buffer, 1, recv_bytes, fp);
			recv_bytes = recv(client->cinfo.sock, buffer, MAX_BUFFER_SIZE, 0);
		}
		
		if(recv_bytes < 0) {
			DieWithError("recv() failed");
		}

		fclose(fp);
			
		close(client->cinfo.sock);

		delete client;

		cout << "done\n";
		
		return NULL;
	}
};


int main() {
	Server s(NULL, 5000);

	s.start();

	while(1) {
		client_info cinfo = s.client();		

		cout << "handling client address " << inet_ntoa(cinfo.addr.sin_addr) << endl;

		ClientHandler *handler = new ClientHandler(cinfo);

		pthread_t t;
		int rc = pthread_create(&t, NULL, ClientHandler::handle, (void*)handler);
		if(rc) {
			DieWithError("pthread_create() failed");
		}
	}

	s.end();

	pthread_exit(NULL);
}

