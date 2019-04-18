int main() {
	int a;
	bool b;
	float f;
	char c;

	b = !(false);
	a = 0;
	f = -float(2);
	c = 'b'; 
	b = a <= 4;

	a = a + int(f);

	c = char(int(c) + int(c));
}
