# Resolved store snapshot

FluencyLoop records are append-only. Consumers need a stable current view without changing those
historical files. The snapshot reader in
[[src/main/java/io/github/baokhang83/fluencydrift/store/JsonlStoreReader.java]] reads each JSONL
line independently and uses a supported record identity to retain the latest effective value.

It keeps unknown record shapes as unkeyed evidence. It also reports malformed lines with their
source and line number. This preserves useful local evidence and makes incomplete input visible to
later comparison tools.
