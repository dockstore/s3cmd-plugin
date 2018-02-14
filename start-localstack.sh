localstack start > localstack.log &
while ! less localstack.log | grep Ready. ; do
  sleep 1
done
echo 'done'

