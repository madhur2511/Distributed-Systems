rm logs
make test
for i in $(seq 1 100);
do
    echo $i
    java -cp ".:reference-rmi.jar" conformance.ConformanceTests >> logs
done

grep -o "passed: 18" logs | wc -l
