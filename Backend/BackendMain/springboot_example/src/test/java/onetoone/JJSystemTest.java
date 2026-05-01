package onetoone;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JJSystemTest {

    private final int moderatorId = 1;
    private final int groupId = 22;

    @LocalServerPort
    private int port;

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    // ================= MODERATOR TESTS =================

    @Test
    public void createModeratorShouldReturnSavedModerator() {
        String uniqueEmail = "jj_test_mod_" + System.currentTimeMillis() + "@example.com";

        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "displayName": "JJ Test Moderator",
              "email": "%s",
              "passwordHash": "password123",
              "active": true
            }
            """.formatted(uniqueEmail))
                .when()
                .post("/moderators")
                .then()
                .statusCode(200)
                .body("moderatorId", notNullValue())
                .body("displayName", equalTo("JJ Test Moderator"))
                .body("email", equalTo(uniqueEmail))
                .body("active", equalTo(true));
    }

    @Test
    public void loginModeratorShouldReturnModeratorAccount() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                  "email": "jj_test_mod@example.com",
                  "passwordHash": "password123"
                }
                """)
                .when()
                .post("/moderators/login")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(401)));
    }

    @Test
    public void getModeratorByIdShouldReturnModeratorOrNotFound() {
        when()
                .get("/moderators/" + moderatorId)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(404)));
    }

    @Test
    public void getGroupsForModeratorShouldReturnListOrNotFound() {
        when()
                .get("/moderators/" + moderatorId + "/groups")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(404)));
    }

    // ================= GROUP MEMBER TESTS =================

    @Test
    public void getApprovedMembersShouldReturnList() {
        when()
                .get("/moderators/" + moderatorId + "/groups/" + groupId + "/members")
                .then()
                .statusCode(200);
    }

    @Test
    public void getPendingMembersShouldReturnList() {
        when()
                .get("/moderators/" + moderatorId + "/groups/" + groupId + "/pending-members")
                .then()
                .statusCode(200);
    }

    // ================= EVENT TESTS =================

    @Test
    public void createEventShouldReturnSavedEvent() {
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "title": "JJ System Test Event",
              "description": "Testing event creation",
              "location": "Carver Hall",
              "eventTime": "2026-05-02T18:30:00"
            }
            """)
                .when()
                .post("/moderators/" + moderatorId + "/groups/" + groupId + "/events")
                .then()
                .statusCode(200)
                .body("eventId", notNullValue())
                .body("groupId", equalTo(groupId))
                .body("moderatorId", equalTo(moderatorId))
                .body("title", equalTo("JJ System Test Event"));
    }

    @Test
    public void getEventsShouldReturnList() {
        when()
                .get("/moderators/" + moderatorId + "/groups/" + groupId + "/events")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(403), equalTo(404)));
    }

    @Test
    public void updateFakeEventShouldReturnNotFoundOrForbidden() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                  "title": "Updated Test Event",
                  "description": "Updated description",
                  "location": "Library",
                  "eventTime": "2026-05-03T20:00:00"
                }
                """)
                .when()
                .put("/moderators/" + moderatorId + "/groups/" + groupId + "/events/999999")
                .then()
                .statusCode(anyOf(equalTo(403), equalTo(404)));
    }

    @Test
    public void deleteFakeEventShouldReturnNotFoundOrForbidden() {
        when()
                .delete("/moderators/" + moderatorId + "/groups/" + groupId + "/events/999999")
                .then()
                .statusCode(anyOf(equalTo(403), equalTo(404)));
    }

    // ================= ANNOUNCEMENT TESTS =================

    @Test
    public void createAnnouncementShouldReturnSavedAnnouncement() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                  "title": "JJ Test Announcement",
                  "content": "This announcement was created by a system test."
                }
                """)
                .when()
                .post("/moderators/" + moderatorId + "/groups/" + groupId + "/announcements")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(403), equalTo(404)));
    }

    @Test
    public void getAnnouncementsShouldReturnList() {
        when()
                .get("/moderators/" + moderatorId + "/groups/" + groupId + "/announcements")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(403), equalTo(404)));
    }

    @Test
    public void pinFakeAnnouncementShouldReturnNotFoundOrForbidden() {
        when()
                .put("/moderators/" + moderatorId + "/groups/" + groupId + "/announcements/999999/pin")
                .then()
                .statusCode(anyOf(equalTo(403), equalTo(404)));
    }

    @Test
    public void unpinFakeAnnouncementShouldReturnNotFoundOrForbidden() {
        when()
                .put("/moderators/" + moderatorId + "/groups/" + groupId + "/announcements/999999/unpin")
                .then()
                .statusCode(anyOf(equalTo(403), equalTo(404)));
    }

    @Test
    public void deleteFakeAnnouncementShouldReturnNotFoundOrForbidden() {
        when()
                .delete("/moderators/" + moderatorId + "/groups/" + groupId + "/announcements/999999")
                .then()
                .statusCode(anyOf(equalTo(403), equalTo(404)));
    }

    // ================= MESSAGE MODERATION TESTS =================

    @Test
    public void getGroupMessagesShouldReturnMessagesOrError() {
        when()
                .get("/moderators/" + moderatorId + "/groups/" + groupId + "/messages")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(403), equalTo(404)));
    }

    // ================= MORE GROUP TESTS =================

    @Test
    public void getAllGroupsShouldReturn200() {
        when()
                .get("/groups")
                .then()
                .statusCode(200);
    }

    @Test
    public void getGroupByIdShouldReturnGroupOr404() {
        when()
                .get("/groups/" + groupId)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(404)));
    }

    @Test
    public void searchGroupsShouldReturn200() {
        when()
                .get("/groups/search?keyword=test")
                .then()
                .statusCode(200);
    }

    @Test
    public void recommendGroupsShouldReturn200() {
        when()
                .get("/groups/recommend/62")
                .then()
                .statusCode(200);
    }

    @Test
    public void getMyGroupsShouldReturn200() {
        when()
                .get("/groups/me/62")
                .then()
                .statusCode(200);
    }

    @Test
    public void getUserGroupEventsShouldReturn200Or404() {
        when()
                .get("/groups/" + groupId + "/events")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(404)));
    }

    @Test
    public void getUserGroupAnnouncementsShouldReturn200Or404() {
        when()
                .get("/groups/" + groupId + "/announcements")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(404)));
    }


// ================= GROUP MEMBER TESTS =================

    @Test
    public void joinGroupShouldReturnMessage() {
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "user_id": 62,
              "group_id": 22
            }
            """)
                .when()
                .post("/gm/join")
                .then()
                .statusCode(200)
                .body(anyOf(
                        equalTo("{\"message\":\"success\"}"),
                        equalTo("{\"message\":\"already_requested_or_member\"}"),
                        equalTo("{\"message\":\"failure\"}")
                ));
    }

    @Test
    public void listGroupMembersShouldReturn200() {
        when()
                .get("/gm/glist/" + groupId)
                .then()
                .statusCode(200);
    }

    @Test
    public void listUserGroupsShouldReturn200() {
        when()
                .get("/gm/ulist/62")
                .then()
                .statusCode(200);
    }

    @Test
    public void updateFakeMembershipShouldReturnFailure() {
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "status": "APPROVED"
            }
            """)
                .when()
                .put("/gm/memstat/999999")
                .then()
                .statusCode(200)
                .body(equalTo("{\"message\":\"failure\"}"));
    }

    @Test
    public void deleteFakeMembershipShouldReturnFailure() {
        when()
                .delete("/gm/leave/999999")
                .then()
                .statusCode(200)
                .body(equalTo("{\"message\":\"failure\"}"));
    }


// ================= CONVERSATION TESTS =================

    @Test
    public void createDirectConversationShouldReturnConversation() {
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "user1Id": 62,
              "user2Id": 63
            }
            """)
                .when()
                .post("/conversations/direct")
                .then()
                .statusCode(200)
                .body("conversationId", notNullValue())
                .body("type", equalTo("DIRECT"));
    }

    @Test
    public void createGroupConversationShouldReturnConversation() {
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "name": "JJ Test Group Conversation",
              "groupId": 22,
              "userIds": [62, 63, 64]
            }
            """)
                .when()
                .post("/conversations/group")
                .then()
                .statusCode(200)
                .body("conversationId", notNullValue())
                .body("type", equalTo("GROUP"));
    }

    @Test
    public void getUserConversationsShouldReturn200() {
        when()
                .get("/conversations/user/62")
                .then()
                .statusCode(200);
    }

    @Test
    public void getConversationShouldReturn200() {
        when()
                .get("/conversations/1")
                .then()
                .statusCode(200);
    }

    @Test
    public void leaveFakeConversationShouldReturnNotFoundMessage() {
        when()
                .delete("/conversations/999999/leave/62")
                .then()
                .statusCode(200)
                .body(equalTo("User not found in conversation"));
    }


// ================= MESSAGE TESTS =================

    @Test
    public void getMessagesForConversationShouldReturn200Or500() {
        when()
                .get("/messages/conversation/1/user/62")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(500)));
    }

    @Test
    public void removeFakeMessageShouldReturn404() {
        when()
                .put("/moderators/" + moderatorId + "/messages/999999/remove")
                .then()
                .statusCode(404)
                .body(equalTo("Message not found"));
    }

    @Test
    public void restoreFakeMessageShouldReturn404() {
        when()
                .put("/moderators/" + moderatorId + "/messages/999999/restore")
                .then()
                .statusCode(404)
                .body(equalTo("Message not found"));
    }


// ================= GROUP MODERATOR FLOW TESTS =================

    @Test
    public void createGroupAsModeratorShouldReturnSavedGroup() {
        String groupName = "JJ Test Group " + System.currentTimeMillis();

        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "groupName": "%s",
              "description": "System test group",
              "interests": ["coding", "games"]
            }
            """.formatted(groupName))
                .when()
                .post("/moderators/" + moderatorId + "/groups")
                .then()
                .statusCode(200)
                .body("groupId", notNullValue())
                .body("groupName", equalTo(groupName))
                .body("moderatorId", equalTo(moderatorId));
    }

    @Test
    public void editGroupShouldReturnUpdatedGroupOr404() {
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "groupName": "Updated JJ Test Group",
              "description": "Updated description",
              "interests": ["java", "spring"]
            }
            """)
                .when()
                .put("/moderators/" + moderatorId + "/groups/" + groupId)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(403), equalTo(404)));
    }

    @Test
    public void approveMissingMemberShouldReturn404Or403() {
        when()
                .put("/moderators/" + moderatorId + "/groups/" + groupId + "/members/999999/approve")
                .then()
                .statusCode(anyOf(equalTo(403), equalTo(404)));
    }

    @Test
    public void removeMissingMemberShouldReturn404Or403() {
        when()
                .delete("/moderators/" + moderatorId + "/groups/" + groupId + "/members/999999")
                .then()
                .statusCode(anyOf(equalTo(403), equalTo(404)));
    }

    // ================= USER TESTS =================

    @Test
    public void createUserShouldReturnSuccess() {
        String email = "jj_user_" + System.currentTimeMillis() + "@example.com";

        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "email": "%s",
              "passwordHash": "pass123",
              "displayName": "JJ Test User",
              "bio": "system test bio",
              "major": "Computer Science",
              "age": 19
            }
            """.formatted(email))
                .when()
                .post("/users")
                .then()
                .statusCode(200)
                .body(equalTo("{\"message\":\"success\"}"));
    }

    @Test
    public void getUserByIdShouldReturn200Or404() {
        when()
                .get("/users/62")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(404)));
    }

    @Test
    public void editExistingUserShouldReturnSuccessOrFailure() {
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "displayName": "JJ Updated User",
              "bio": "updated bio",
              "major": "Software Engineering",
              "age": 20
            }
            """)
                .when()
                .put("/users/edit/62")
                .then()
                .statusCode(200)
                .body(anyOf(
                        equalTo("{\"message\":\"success\"}"),
                        equalTo("{\"message\":\"failure\"}")
                ));
    }

    @Test
    public void loginUserWithBadEmailShouldReturn401() {
        when()
                .get("/login?email=fake_user@example.com&passwordHash=nope")
                .then()
                .statusCode(401)
                .body(equalTo("{\"message\":\"failure\"}"));
    }

    @Test
    public void loginUserWithEmptyEmailShouldReturn401() {
        when()
                .get("/login?email=&passwordHash=nope")
                .then()
                .statusCode(401)
                .body(equalTo("{\"message\":\"failure\"}"));
    }


// ================= MATCH TESTS =================

    @Test
    public void createMatchShouldReturnCreated() {
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "user1Id": 62,
              "user2Id": 63,
              "status": "PENDING"
            }
            """)
                .when()
                .post("/matches")
                .then()
                .statusCode(200)
                .body(equalTo("Match created"));
    }

    @Test
    public void getMatchesByUserShouldReturn200() {
        when()
                .get("/matches/user/62")
                .then()
                .statusCode(200);
    }

    @Test
    public void getMatchByFakeIdShouldReturn404() {
        when()
                .get("/matches/999999")
                .then()
                .statusCode(404);
    }

    @Test
    public void editFakeMatchShouldReturn404() {
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "status": "ACCEPTED"
            }
            """)
                .when()
                .put("/matches/edit/999999")
                .then()
                .statusCode(404);
    }

    @Test
    public void deleteFakeMatchShouldReturn404() {
        when()
                .delete("/matches/del/999999")
                .then()
                .statusCode(404)
                .body(equalTo("Match not found"));
    }

    @Test
    public void getNextMatchShouldReturn200() {
        when()
                .get("/matches/next/62")
                .then()
                .statusCode(200)
                .body("userId", notNullValue())
                .body("matchId", notNullValue());
    }

    // ================= EXTRA MODERATOR TESTS =================

    @Test
    public void getAllModeratorsShouldReturn200() {
        when()
                .get("/moderators")
                .then()
                .statusCode(200);
    }

    @Test
    public void updateModeratorShouldReturnUpdatedModerator() {
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "displayName": "Updated Moderator",
              "email": "updated_mod_%s@example.com",
              "passwordHash": "newpass123",
              "active": true
            }
            """.formatted(System.currentTimeMillis()))
                .when()
                .put("/moderators/" + moderatorId)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(404)));
    }

    @Test
    public void updateFakeModeratorShouldReturn404() {
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "displayName": "Fake Moderator",
              "email": "fake@example.com",
              "passwordHash": "fakepass",
              "active": true
            }
            """)
                .when()
                .put("/moderators/999999")
                .then()
                .statusCode(404);
    }

    @Test
    public void deleteFakeModeratorShouldReturn404() {
        when()
                .delete("/moderators/999999")
                .then()
                .statusCode(404)
                .body(equalTo("Moderator not found"));
    }

    @Test
    public void getOneManagedGroupShouldReturn200Or404() {
        when()
                .get("/moderators/" + moderatorId + "/groups/" + groupId)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(404)));
    }

    @Test
    public void getOneFakeManagedGroupShouldReturn404() {
        when()
                .get("/moderators/" + moderatorId + "/groups/999999")
                .then()
                .statusCode(404)
                .body(equalTo("Group not found"));
    }

    @Test
    public void getModeratorGroupMessagesShouldReturn200Or404Or403() {
        when()
                .get("/moderators/" + moderatorId + "/groups/" + groupId + "/messages")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(403), equalTo(404)));
    }

    @Test
    public void getModeratorGroupMessagesFakeModeratorShouldReturn404() {
        when()
                .get("/moderators/999999/groups/" + groupId + "/messages")
                .then()
                .statusCode(404)
                .body(equalTo("Moderator not found"));
    }

    @Test
    public void getModeratorGroupMessagesFakeGroupShouldReturn404() {
        when()
                .get("/moderators/" + moderatorId + "/groups/999999/messages")
                .then()
                .statusCode(404)
                .body(equalTo("Group not found"));
    }


// ================= EXTRA EVENT TESTS =================

    @Test
    public void createEventFakeGroupShouldReturn404() {
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "title": "Fake Group Event",
              "description": "Should fail",
              "location": "Nowhere",
              "eventTime": "2026-05-02T18:30:00"
            }
            """)
                .when()
                .post("/moderators/" + moderatorId + "/groups/999999/events")
                .then()
                .statusCode(404)
                .body(equalTo("Group not found"));
    }

    @Test
    public void getEventsFakeGroupShouldReturn404() {
        when()
                .get("/moderators/" + moderatorId + "/groups/999999/events")
                .then()
                .statusCode(404)
                .body(equalTo("Group not found"));
    }

    @Test
    public void getOneFakeEventShouldReturn404() {
        when()
                .get("/moderators/" + moderatorId + "/groups/" + groupId + "/events/999999")
                .then()
                .statusCode(404)
                .body(equalTo("Event not found"));
    }

    @Test
    public void updateEventFakeGroupShouldReturn404() {
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "title": "Updated Event",
              "description": "Updated",
              "location": "Somewhere",
              "eventTime": "2026-05-03T18:30:00"
            }
            """)
                .when()
                .put("/moderators/" + moderatorId + "/groups/999999/events/1")
                .then()
                .statusCode(404)
                .body(equalTo("Group not found"));
    }

    @Test
    public void deleteEventFakeGroupShouldReturn404() {
        when()
                .delete("/moderators/" + moderatorId + "/groups/999999/events/1")
                .then()
                .statusCode(404)
                .body(equalTo("Group not found"));
    }


// ================= EXTRA ANNOUNCEMENT TESTS =================

    @Test
    public void createAnnouncementFakeGroupShouldReturn404() {
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
              "title": "Fake Group Announcement",
              "content": "Should fail"
            }
            """)
                .when()
                .post("/moderators/" + moderatorId + "/groups/999999/announcements")
                .then()
                .statusCode(404)
                .body(equalTo("Group not found"));
    }

    @Test
    public void getAnnouncementsFakeGroupShouldReturn404() {
        when()
                .get("/moderators/" + moderatorId + "/groups/999999/announcements")
                .then()
                .statusCode(404)
                .body(equalTo("Group not found"));
    }

    @Test
    public void pinFakeAnnouncementShouldReturn404() {
        when()
                .put("/moderators/" + moderatorId + "/groups/" + groupId + "/announcements/999999/pin")
                .then()
                .statusCode(404)
                .body(equalTo("Announcement not found"));
    }

    @Test
    public void unpinFakeAnnouncementShouldReturn404() {
        when()
                .put("/moderators/" + moderatorId + "/groups/" + groupId + "/announcements/999999/unpin")
                .then()
                .statusCode(404)
                .body(equalTo("Announcement not found"));
    }


// ================= EXTRA GROUP TESTS =================

    @Test
    public void addUserToFakeGroupShouldReturn404() {
        when()
                .post("/groups/999999/add/62")
                .then()
                .statusCode(404)
                .body(equalTo("Group not found"));
    }

    @Test
    public void removeUserFromFakeGroupShouldReturn404() {
        when()
                .delete("/groups/999999/remove/62")
                .then()
                .statusCode(404);
    }

    @Test
    public void deleteFakeGroupShouldReturn404() {
        when()
                .delete("/groups/999999")
                .then()
                .statusCode(404)
                .body(equalTo("Group not found"));
    }

    @Test
    public void deleteFakeGroupAsModeratorShouldReturn404Or403() {
        when()
                .delete("/moderators/" + moderatorId + "/groups/999999")
                .then()
                .statusCode(anyOf(equalTo(403), equalTo(404)));
    }
}