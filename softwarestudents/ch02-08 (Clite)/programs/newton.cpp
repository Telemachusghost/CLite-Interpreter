int main() {
	float a, x, result;
	bool f;
	a = 4.0;
	x = 1.0;
	f = x*x > a+0.0001;
	x = (x + a/x)/2.0;
	f = x*x > a+0.0001 || x*x < a-0.0001;

	while (x*x > a+0.0001 || x*x < a-0.0001 )
		x = (x + a/x)/2.0;
	result = x;
}
