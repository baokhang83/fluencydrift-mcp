# Root-confined repository snapshot

Repository inspection starts from one supplied root. The observer in
[[src/main/java/io/github/baokhang83/fluencydrift/repository/RepositoryObserver.java]] resolves that
root to its canonical path. It skips `.git`, accepts only regular non-symbolic-link files, and stores
each observed path relative to the root.

The snapshot also contains Git tracking information from
[[src/main/java/io/github/baokhang83/fluencydrift/repository/GitCliTrackingReader.java]]. If Git
cannot provide that state, the snapshot carries a diagnostic and is incomplete. The boundary follows
[[§5]] so a repository request cannot expose files outside its root.
