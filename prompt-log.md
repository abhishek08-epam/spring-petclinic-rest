###prompt1
1. Open VisitRestController.java and list any missing edge-case checks — specifically visits scheduled in the past, duplicate visit entries, and visits linked to pet IDs that don't exist in the system.
###prompt2
2. For this controller, write parameterized test cases using JUnit 5's @ParameterizedTest annotation combined with @CsvSource, so multiple input scenarios can be tested without repeating code.
###prompt3
3. Add test coverage using MockMvc that confirms the API responds with a 400 Bad Request status whenever someone tries to schedule a visit with a date that has already passed.
###prompt4
4. Rewrite the assertion statements in these tests to use AssertJ's assertThat(...) syntax instead of standard JUnit assertions, for better readability.
###prompt5
5. Set up mock data for the service layer using Mockito — stub out clinicService.findPetById(...) so the tests don't depend on a real database.1. **Missing Edge-Case Checks in VisitRestController.java**:
   - Check for visits scheduled in the past: Ensure that the controller validates the visit date and returns an error if the date is before the current date.
   - Check for duplicate visit entries: Implement logic to check if a visit with the same pet ID and date already exists, and return an appropriate error response.
   - Check for visits linked to non-existent pet IDs: Validate that the provided pet ID exists in the system before scheduling a visit, returning an error if it does not.
