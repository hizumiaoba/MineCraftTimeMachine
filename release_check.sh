LATEST_TAG=$(git describe --tags --abbrev=0)

PREVIOUS_TAG_RAW=$(git describe --tags "$(git rev-list --tags --skip=1 --max-count=1)")

if echo "$PREVIOUS_TAG_RAW" | grep -q -e 'alpha' -e 'beta'; then
  PREVIOUS_TAG=$(echo "$PREVIOUS_TAG_RAW" | cut -d'-' -f1-2)
else
  PREVIOUS_TAG=$(echo "$PREVIOUS_TAG_RAW" | cut -d'-' -f1)
fi

CHANGES=$(git log "$PREVIOUS_TAG".."$LATEST_TAG" --pretty=format:"- %s by %an (%h)" --no-merges)
echo "LATEST_TAG=$LATEST_TAG"
echo "PREVIOUS_TAG=$PREVIOUS_TAG"
echo "CHANGES=$CHANGES"
echo "::set-output name=changelog::$CHANGES"
