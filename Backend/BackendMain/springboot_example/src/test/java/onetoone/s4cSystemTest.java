package onetoone;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)

@RunWith(SpringRunner.class)
@SpringBootTest(classes = onetoone.Main.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class s4cSystemTest {
    @Autowired
    private MockMvc controller;

    private static long testUserId;
    private static long testAdminId;
    private static long testGroupId;
    private static boolean setupDone = false;

    @Before
    public void setup() throws Exception {
        if (setupDone) return;
        setupDone = true;

        controller.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"testadmin@iastate.edu\",\"passwordHash\":\"testpass\",\"displayName\":\"Test Admin\"}"))
                .andExpect(status().isOk());

        MvcResult allUsersResult = controller.perform(get("/users/all"))
                .andExpect(status().isOk())
                .andReturn();

        System.out.println(allUsersResult.getResponse().getContentAsString());

        List<Integer> userIds = JsonPath.read(
                allUsersResult.getResponse().getContentAsString(),
                "$[?(@.displayName == 'Test Admin')].userID"
        );
        testUserId = userIds.get(0).longValue();

        controller.perform(post("/admin/" + testUserId))
                .andExpect(status().isOk());

        MvcResult adminResult = controller.perform(get("/admin/all"))
                .andExpect(status().isOk())
                .andReturn();

        List<Integer> adminIds = JsonPath.read(
                adminResult.getResponse().getContentAsString(),
                "$[?(@.userId.userId == " + testUserId + ")].adminId"
        );
        testAdminId = adminIds.get(0).longValue();

        MvcResult groupResult = controller.perform(post("/moderators/1/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"groupName\":\"Test Group\",\"description\":\"A test group\"}"))
                .andExpect(status().isOk())
                .andReturn();

        testGroupId = ((Integer) JsonPath.read(
                groupResult.getResponse().getContentAsString(), "$.groupId")).longValue();
    }

    //        -------------USER STATUS-------------

    @Test
    public void approveUser() throws Exception {
        String bb = "{\"status\":\"APPROVED\"}";

        controller.perform(put("/users/edit/79")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bb))
            .andExpect(status().isOk());
    }

    @Test
    public void testLoginSuspendedUser() throws Exception {
        controller.perform(get("/login")
                        .param("email", "tester2@iastate.edu")
                        .param("passwordHash", "abc123"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testLoginUnapprovedUser() throws Exception {
        controller.perform(get("/login")
                        .param("email", "tester3@iastate.edu")
                        .param("passwordHash", "abc123"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void unverifiedUsers() throws Exception {
        controller.perform(get("/users/status/NEED_APPROVAL?requesterID=4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].userId", hasItem(61)));
    }

    //        -------------ADMINS-------------

    @Test
    public void test1createAdmin() throws Exception {
        controller.perform(post("/admin/" + testUserId))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void test2verifyAdmin() throws Exception {
        controller.perform(get("/admin/" + testUserId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void test3adminStatus() throws Exception {
        String bb = "{\"activeAdmin\":\"false\"}";

        controller.perform(put("/admin/" + testAdminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bb))
                .andExpect(status().isOk());

        controller.perform(get("/admin/" + testUserId))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    public void test4deleteAdmin() throws Exception {
        controller.perform(delete("/admin/" + testAdminId))
                .andExpect(status().isOk());

        controller.perform(get("/admin/" + testUserId))
                .andExpect(status().isNotFound());
    }

    //        -------------REPORTS-------------

    private static long testReportId;

    @Test
    public void test7createReport() throws Exception {
        String body = "{\"reporterId\":{\"userId\":" + testUserId + "},\"reportedId\":{\"userId\":79},\"reason\":\"Test report\",\"description\":\"Test description\"}";

        controller.perform(post("/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        MvcResult allReports = controller.perform(get("/reports/all"))
                .andExpect(status().isOk())
                .andReturn();

        List<Integer> reportIds = JsonPath.read(
                allReports.getResponse().getContentAsString(),
                "$[?(@.reporterId.userId == " + testUserId + ")].reportId"
        );
        testReportId = reportIds.get(reportIds.size() - 1).longValue();
    }

    @Test
    public void test7createReportInvalidReporter() throws Exception {
        String body = "{\"reporterId\":{\"userId\":99999},\"reportedId\":{\"userId\":79},\"reason\":\"Test report\"}";

        controller.perform(post("/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void test8editReportStatus() throws Exception {
        String body = "{\"status\":\"APPROVED\"}";

        controller.perform(put("/reports/" + testReportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        controller.perform(get("/reports/" + testReportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    public void test9deleteReport() throws Exception {
        controller.perform(delete("/reports/" + testReportId))
                .andExpect(status().isOk());

        controller.perform(get("/reports/" + testReportId))
                .andExpect(status().isNotFound());
    }

    //        -------------IMAGES-------------

    private static long testImageId;

    @Test
    public void utest1uploadImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        MvcResult result = controller.perform(multipart("/image/user/" + testUserId)
                        .file(file))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult imagesResult = controller.perform(get("/image/user/" + testUserId))
                .andExpect(status().isOk())
                .andReturn();

        List<Integer> imageIds = JsonPath.read(
                imagesResult.getResponse().getContentAsString(),
                "$[*].imageId"
        );
        testImageId = imageIds.get(imageIds.size() - 1).longValue();
    }

    @Test
    public void utest1uploadImageInvalidUser() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        controller.perform(multipart("/image/99999")
                        .file(file))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void utest1uploadImageWithoutFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        controller.perform(multipart("/image/user/" + testUserId)
                        .file(emptyFile))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void utest2getImageLink() throws Exception {
        controller.perform(get("/image/" + testImageId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/files/images/" + testUserId)));
    }

    @Test
    public void utest3deleteImage() throws Exception {
        controller.perform(delete("/image/" + testImageId))
                .andExpect(status().isOk());

        controller.perform(get("/image/" + testImageId))
                .andExpect(status().is4xxClientError());
    }

    //        -------------MATCHES-------------

    private static long testMatchId;

    @Test
    public void utest4createMatch() throws Exception {
        String body = "{\"user1Id\":" + testUserId + ",\"user2Id\":79,\"status\":\"PENDING\"}";

        controller.perform(post("/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        MvcResult result = controller.perform(get("/matches/user/" + testUserId))
                .andExpect(status().isOk())
                .andReturn();

        List<Integer> matchIds = JsonPath.read(
                result.getResponse().getContentAsString(),
                "$[?(@.user1Id == " + testUserId + ")].matchId"
        );
        testMatchId = matchIds.get(matchIds.size() - 1).longValue();
    }

    @Test
    public void utest5editMatchStatus() throws Exception {
        String body = "{\"status\":\"ACCEPTED\"}";

        controller.perform(put("/matches/edit/" + testMatchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        controller.perform(get("/matches/" + testMatchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    public void utest5editMatchError() throws Exception {
        String body = "{\"status\":\"ACCEPTED\"}";

        controller.perform(put("/matches/edit/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    public void utest6deleteMatch() throws Exception {
        controller.perform(delete("/matches/del/" + testMatchId))
                .andExpect(status().isOk());

        controller.perform(get("/matches/" + testMatchId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void utest6deleteMatchError() throws Exception {
        controller.perform(delete("/matches/del/99999"))
                .andExpect(status().is4xxClientError());
    }

    //        -------------GROUP MEMBERS-------------

    @Test
    public void vtest1joinGroup() throws Exception {
        String body = "{\"user_id\":" + testUserId + ",\"group_id\":" + testGroupId + "}";

        controller.perform(post("/gm/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("success")));

        controller.perform(post("/gm/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(content().string(containsString("already_requested_or_member")));

        MvcResult membersResult = controller.perform(get("/gm/glist/" + testGroupId))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult approveResult = controller.perform(
                        put("/moderators/1/groups/" + testGroupId + "/members/" + testUserId + "/approve"))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult approvedList = controller.perform(get("/gm/glist/" + testGroupId))
                .andExpect(status().isOk())
                .andReturn();

        List<Integer> userIds = JsonPath.read(
                approvedList.getResponse().getContentAsString(),
                "$[*].userId"
        );
        assert userIds.contains((int) testUserId);
    }

    @Test
    public void vtest1joinGroupError() throws Exception {
        String body = "{\"user_id\":99999,\"group_id\":" + testGroupId + "}";

        controller.perform(post("/gm/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(content().string(containsString("failure")));
    }

    @Test
    public void vtest2listUserGroups() throws Exception {
        controller.perform(get("/gm/ulist/" + testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].groupId", hasItem((int) testGroupId)));
    }

    @Test
    public void vtest3leaveGroup() throws Exception {
        controller.perform(delete("/groups/" + testGroupId + "/leave/" + testUserId))
                .andExpect(status().isOk());

        controller.perform(get("/gm/glist/" + testGroupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].userId", not(hasItem((int) testUserId))));
    }

    @Test
    public void vtest4deleteGroup() throws Exception {
        controller.perform(delete("/moderators/1/groups/" + testGroupId))
                .andExpect(status().isOk());

        controller.perform(get("/groups/" + testGroupId))
                .andExpect(status().isNotFound());
    }

    //        -------------NOTIFICATIONS-------------

    private static long testNotificationId;

    @Test
    public void vtest5getNotifications() throws Exception {
        MvcResult result = controller.perform(get("/notifications/" + testUserId))
                .andExpect(status().isOk())
                .andReturn();

        List<Integer> notifIds = JsonPath.read(result.getResponse().getContentAsString(), "$[*].id");
        testNotificationId = notifIds.get(0).longValue();
    }

    @Test
    public void vtest6deleteNotification() throws Exception {
        controller.perform(delete("/notifications/" + testNotificationId))
                .andExpect(status().isOk());

        controller.perform(get("/notifications/" + testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", not(hasItem((int) testNotificationId))));
    }

    @Test
    public void vtest6deleteNotificationError() throws Exception {
        controller.perform(delete("/notifications/99999"))
                .andExpect(status().is4xxClientError());
    }

    //        -------------OTHER STUFF-------------

    @Test
    public void numOfUnresolvedReports() throws Exception {
        controller.perform(get("/reports/count/pending"))
                .andExpect(status().isOk());
    }

    @Test
    public void zzzzzz_deleteTestUser() throws Exception {
        controller.perform(delete("/users/" + testUserId))
                .andExpect(status().isOk());
    }
}
