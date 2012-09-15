#include "headers.cpp"
#include "cpu.cpp"

Statistics* read_stat_file(const char *fname) {
	Statistics *stat = new Statistics();
	
	FILE *fp = fopen(fname, "rb");
	
	fseek(fp, 0, SEEK_END);
	int fsize = ftell(fp);
	rewind(fp);	
	
	char *ptr = (char*)(&stat->d);
	int total_read = fread(ptr, 1, fsize, fp);
	
	fclose(fp);
	
	if(total_read != fsize) {
		DieWithError("can't read file", __FILE__, __LINE__);
		delete stat;		
		return NULL;
	}	
	
	return stat;
}

int main() {

	Statistics *stat = read_stat_file("../stat/machine-16.19.64.0-1322078896.bin");
	if(stat) {
		stat->print();
		delete stat;
	}
	
	return 0;
}
