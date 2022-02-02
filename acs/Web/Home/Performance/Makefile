#
# This is the Performance ACS Web Page Makefile.
#
.PHONY: performance
performance:
	@echo "====> Updating historical performance pages from cvs archive"
	@rm -rf BenchmarkDoc
	@cvs -Q export -r HEAD -d BenchmarkDoc ACS/Benchmark/components/doc/
	@chmod -R ugo+r BenchmarkDoc
	@echo "=== Done ==="

all: performance

clean: 
	@rm -rf BenchmarkDoc *~
	@echo "=== Done ==="

# __oOo__