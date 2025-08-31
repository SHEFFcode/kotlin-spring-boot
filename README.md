1. **Start your application** with the dev profile:
``` bash
   ./gradlew bootRun --args='--spring.profiles.active=dev'
```
1. **Use the HTTP scratch file**:
    - Open `scratch/todo-api-tests.http` in IntelliJ IDEA
    - Click the green play button next to each request to execute it
    - Results will appear in the HTTP Response panel

2. **View seeded data**:
    - Visit `http://localhost:8080/h2-console`
    - Use JDBC URL: `jdbc:h2:mem:tododb`
    - Username: `sa`, Password: `password`

3. **Run the test script** (if you have `curl` and `jq` installed):
``` bash
   chmod +x scripts/test-api.sh
   ./scripts/test-api.sh
```
4. Run Tests
```bash
./gradlew test

./gradlew unitTest

./gradlew test jacocoTestReport

```