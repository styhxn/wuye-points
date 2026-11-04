package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.controller.ActivityGroupController;
import com.wuye.points.dto.activity.ActivityGroupBuildingLeaderListResponse;
import com.wuye.points.entity.ActivityGroup;
import com.wuye.points.entity.ActivityGroupMember;
import com.wuye.points.entity.BuildingLeader;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.ActivityGroupMemberRepository;
import com.wuye.points.repository.ActivityGroupRepository;
import com.wuye.points.repository.BuildingLeaderRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.RoleGuard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

/**
 * 活动组内单元楼长列表（只读）测试。
 *
 * <p>楼长是物业侧身份，组长只能看不能改：这里同时锁住「组长侧无写接口」这条约束。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
@DisplayName("ActivityGroupService 组内楼长列表测试")
class ActivityGroupBuildingLeaderTest {

    @Mock
    private ActivityGroupRepository activityGroupRepository;
    @Mock
    private ActivityGroupMemberRepository activityGroupMemberRepository;
    @Mock
    private BuildingLeaderRepository buildingLeaderRepository;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private RoleGuard roleGuard;

    @InjectMocks
    private ActivityGroupService service;

    private ActivityGroup group;
    private Resident caller;

    @BeforeEach
    void setUp() {
        group = new ActivityGroup();
        group.setId("ag_demo01");
        group.setName("跑步俱乐部");
        group.setLeaderId("res_leader");
        group.setPropertyCompanyId("pc_demo01");
        group.setStatus("active");
        group.setReviewStatus("approved");

        caller = resident("res_member1", "组员甲");
        when(activityGroupRepository.findById("ag_demo01")).thenReturn(Optional.of(group));
        when(residentRepository.findById("res_member1")).thenReturn(Optional.of(caller));
        UserContext.set(UserContext.builder().userId("res_member1").role("resident").build());
    }

    @AfterEach
    void cleanup() {
        UserContext.clear();
    }

    @Test
    @DisplayName("只返回组内启用中的楼长，普通成员与组长不带入")
    void listsOnlyActiveLeadersAmongGroupMembers() {
        when(activityGroupMemberRepository.findByGroupIdAndStatus("ag_demo01", "active")).thenReturn(List.of(
                member("agm_1", "res_leader"),
                member("agm_2", "res_bl1"),
                member("agm_3", "res_plain")));
        when(buildingLeaderRepository.findByResidentIdInAndPropertyCompanyIdAndStatus(
                anyCollection(), eq("pc_demo01"), eq("active")))
                .thenReturn(List.of(leader("bl_1", "res_bl1")));
        when(residentRepository.findAllById(any())).thenReturn(List.of(resident("res_bl1", "王楼长")));

        ActivityGroupBuildingLeaderListResponse response = service.listGroupBuildingLeaders("ag_demo01");

        assertEquals("ag_demo01", response.getGroupId());
        assertEquals(1, response.getBuildingLeaderCount());
        assertEquals(1, response.getList().size());
        assertEquals("res_bl1", response.getList().get(0).getResidentId());
        assertEquals("agm_2", response.getList().get(0).getMemberId());
        assertEquals("王楼长", response.getList().get(0).getName());
        assertEquals("building_leader", response.getList().get(0).getLeaderRole());
    }

    @Test
    @DisplayName("楼长查询按活动组所属物业公司过滤，不按调用者")
    void scopesLeaderLookupToGroupPropertyCompany() {
        when(activityGroupMemberRepository.findByGroupIdAndStatus("ag_demo01", "active"))
                .thenReturn(List.of(member("agm_2", "res_bl1")));
        when(buildingLeaderRepository.findByResidentIdInAndPropertyCompanyIdAndStatus(
                anyCollection(), any(), any())).thenReturn(List.of());

        service.listGroupBuildingLeaders("ag_demo01");

        ArgumentCaptor<Collection<String>> ids = ArgumentCaptor.forClass(Collection.class);
        verify(buildingLeaderRepository).findByResidentIdInAndPropertyCompanyIdAndStatus(
                ids.capture(), eq("pc_demo01"), eq("active"));
        assertEquals(List.of("res_bl1"), List.copyOf(ids.getValue()));
    }

    @Test
    @DisplayName("组内没有楼长时返回空数组而不是 null")
    void returnsEmptyListWhenNoLeaderInGroup() {
        when(activityGroupMemberRepository.findByGroupIdAndStatus("ag_demo01", "active"))
                .thenReturn(List.of(member("agm_2", "res_plain")));
        when(buildingLeaderRepository.findByResidentIdInAndPropertyCompanyIdAndStatus(
                anyCollection(), any(), any())).thenReturn(List.of());

        ActivityGroupBuildingLeaderListResponse response = service.listGroupBuildingLeaders("ag_demo01");

        assertEquals(0, response.getBuildingLeaderCount());
        assertTrue(response.getList().isEmpty());
    }

    @Test
    @DisplayName("空组不查楼长表")
    void skipsLeaderLookupForEmptyGroup() {
        when(activityGroupMemberRepository.findByGroupIdAndStatus("ag_demo01", "active")).thenReturn(List.of());

        ActivityGroupBuildingLeaderListResponse response = service.listGroupBuildingLeaders("ag_demo01");

        assertEquals(0, response.getBuildingLeaderCount());
        verify(buildingLeaderRepository, never())
                .findByResidentIdInAndPropertyCompanyIdAndStatus(anyCollection(), any(), any());
    }

    @Test
    @DisplayName("跨物业公司读取活动组按不存在处理")
    void rejectsOtherPropertyCompany() {
        caller.setPropertyCompanyId("pc_other");

        BizException ex = assertThrows(BizException.class, () -> service.listGroupBuildingLeaders("ag_demo01"));

        assertEquals(ErrorCode.ACTIVITY_GROUP_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("组长侧只开放读接口，不存在设置/撤销/更新楼长的写接口")
    void leaderSideExposesNoWriteEndpoint() {
        List<String> readPaths = new ArrayList<>();
        List<String> writePaths = new ArrayList<>();
        for (Method method : ActivityGroupController.class.getDeclaredMethods()) {
            String path = mappingPath(method);
            if (path == null) {
                continue;
            }
            if (method.isAnnotationPresent(GetMapping.class)) {
                readPaths.add(path);
            } else {
                writePaths.add(path);
            }
        }

        assertTrue(readPaths.contains("/{id}/building-leaders"), "缺少 GET /activity-groups/{id}/building-leaders");
        assertEquals(List.of(), writePaths.stream()
                .filter(path -> path.contains("building-leader"))
                .toList(), "组长侧不得出现楼长写接口");
    }

    private String mappingPath(Method method) {
        GetMapping get = method.getAnnotation(GetMapping.class);
        if (get != null) {
            return get.value().length > 0 ? get.value()[0] : null;
        }
        PostMapping post = method.getAnnotation(PostMapping.class);
        if (post != null) {
            return post.value().length > 0 ? post.value()[0] : null;
        }
        PutMapping put = method.getAnnotation(PutMapping.class);
        if (put != null) {
            return put.value().length > 0 ? put.value()[0] : null;
        }
        DeleteMapping delete = method.getAnnotation(DeleteMapping.class);
        if (delete != null) {
            return delete.value().length > 0 ? delete.value()[0] : null;
        }
        return null;
    }

    private ActivityGroupMember member(String id, String residentId) {
        ActivityGroupMember member = new ActivityGroupMember();
        member.setId(id);
        member.setGroupId("ag_demo01");
        member.setResidentId(residentId);
        member.setRole("res_leader".equals(residentId) ? "leader" : "member");
        member.setStatus("active");
        return member;
    }

    private BuildingLeader leader(String id, String residentId) {
        BuildingLeader leader = new BuildingLeader();
        leader.setId(id);
        leader.setResidentId(residentId);
        leader.setPropertyCompanyId("pc_demo01");
        leader.setStatus("active");
        return leader;
    }

    private Resident resident(String id, String name) {
        Resident resident = new Resident();
        resident.setId(id);
        resident.setName(name);
        resident.setRole("resident");
        resident.setStatus("active");
        resident.setPropertyCompanyId("pc_demo01");
        return resident;
    }
}
