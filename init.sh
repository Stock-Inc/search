#!/usr/bin/env sh
set -eu

API_URL="${API_URL:-http://localhost:8080/api/v1/documents/upload}"
WORKDIR="${WORKDIR:-./tmp/init-documents}"

mkdir -p "$WORKDIR"

urls='
https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf
https://www.orimi.com/pdf-test.pdf
https://www.adobe.com/support/products/enterprise/knowledgecenter/media/c4611_sample_explain.pdf
https://www.africau.edu/images/default/sample.pdf
https://unec.edu.az/application/uploads/2014/12/pdf-sample.pdf
https://www.learningcontainer.com/wp-content/uploads/2019/09/sample-pdf-file.pdf
https://file-examples.com/storage/fe30e3f98df8b2f0db16a8a/2017/10/file-sample_150kB.pdf
https://www.clickdimensions.com/links/TestPDFfile.pdf
https://www.tutorialspoint.com/pdf/pdf_tutorial.pdf
https://www.irs.gov/pub/irs-pdf/fw4.pdf
'

i=1
printf '%s\n' "$urls" | while IFS= read -r url; do
  [ -n "$url" ] || continue
  file="$WORKDIR/$i.pdf"
  curl -fsSL "$url" -o "$file"
  curl -fsS -X POST "$API_URL" -F "file=@$file"
  i=$((i + 1))
done
