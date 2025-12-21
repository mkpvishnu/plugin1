# Integration Tests for Seasons of Conflict

This directory contains integration tests for critical game flows.

## Test Coverage

### 1. CaptureTickTaskTest
Tests territory capture mechanics:
- Minimum player requirements (3+ players within 10 blocks)
- Capture progress increment (1 per second)
- Capture progress decay (2x speed when requirements not met)
- Enemy contestation (prevents capture)
- Capture completion (300 seconds)
- Capturing team switching
- Dead player filtering
- Cannot capture owned territories

### 2. HealthManagerTest
Tests permadeath and revival mechanics:
- Initial 40 heart setup
- 10 heart loss per death
- Permanent death at 0 health
- Revival limit (2 per cycle)
- Revival health restoration (20 hearts)
- Revival cost (team points)
- Revival reset on cycle advance
- Team elimination detection
- Insufficient points check

### 3. QuestManagerTest
Tests quest system:
- Daily quest generation (3 per player)
- Quest progress tracking
- Quest completion rewards
- Daily reset at midnight
- Team quest collective progress
- Quest type matching
- Weekly quest rewards
- No duplicate quests
- Quest persistence across restarts

## Running Tests

### Current Status: Placeholder Tests

All tests currently contain placeholder implementations with `assertTrue(true, "Test requires MockBukkit setup")`.
To implement these tests, you need to:

1. **Set up MockBukkit for each test**
   ```java
   @BeforeEach
   public void setUp() {
       MockBukkit.mock();
       plugin = MockBukkit.load(SeasonsOfConflict.class);
   }

   @AfterEach
   public void tearDown() {
       MockBukkit.unmock();
   }
   ```

2. **Create mock players, teams, and territories**
   ```java
   Player player = server.addPlayer();
   World world = server.addSimpleWorld("world");
   // etc.
   ```

3. **Replace `assertTrue(true, ...)` with actual test logic**

### Run all tests
```bash
mvn test
```

### Run specific test class
```bash
mvn test -Dtest=CaptureTickTaskTest
```

### Run tests with verbose output
```bash
mvn test -X
```

## Future Improvements

1. **Implement MockBukkit setup** - Replace placeholder tests with real implementations
2. **Add more test cases** - Cover edge cases and error scenarios
3. **Add performance tests** - Verify tasks don't cause lag with many players
4. **Add database tests** - Test data persistence and loading
5. **Add seasonal effects tests** - Test seasonal modifiers work correctly
6. **Add difficulty scaling tests** - Test cycle progression mechanics

## MockBukkit Documentation

- GitHub: https://github.com/MockBukkit/MockBukkit
- Wiki: https://github.com/MockBukkit/MockBukkit/wiki
- Examples: https://github.com/MockBukkit/MockBukkit/tree/v1.20/src/test/java/be/seeseemelk/mockbukkit

## Contributing Tests

When adding new tests:
1. Follow the existing test structure (Arrange → Act → Assert)
2. Use descriptive test names with @DisplayName
3. Group related tests in the same test class
4. Add comments explaining complex setup
5. Ensure tests are isolated (no shared state between tests)
