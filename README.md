# WeblogChallenge

## Requirements

- Spark 2.0.1
- Scala 2.11.8
- SBT 0.13.8
- ~4GB of physical RAM

## Instructions for execution

```bash
git clone https://github.com/aianus/WeblogChallenge.git
cd WeblogChallenge
./run_local.sh
```

## Results
```
2. Average session time: 00:01:40s
3. Average distinct uris per session: 8.31287668266638
4. Most engaged clients (by average session duration):
<IP> <Average session duration>
103.29.159.138 00:34:25s
125.16.218.194 00:34:24s
14.99.226.79 00:34:22s
122.169.141.4 00:34:20s
14.139.220.98 00:34:18s
117.205.158.11 00:34:17s
111.93.89.14 00:34:14s
182.71.63.42 00:34:10s
223.176.3.130 00:34:07s
183.82.103.131 00:34:02s
```
## Notes

I optimized for type-safety, readability, and extensibility over performance

As part of sessionization, all of the requests made by a single client have to fit into memory on a single node. There's definitely a way to do this in a distributed manner but it would have been more complex.

Malformed log lines (about 200 out of >1MM) were ignored. For example, one malformed log line had '{' and '}' characters in the URI

It could use a lot more tests but I ran out of time
