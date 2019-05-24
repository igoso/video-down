cd ..
java  -Xms3192m -Xmx3192m -Xmn1197m -Xss256k -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:SurvivorRatio=8 -jar xdf-demo-1.0-SNAPSHOT.jar