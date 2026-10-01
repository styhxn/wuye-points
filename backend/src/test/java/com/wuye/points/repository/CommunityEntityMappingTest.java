package com.wuye.points.repository;

import com.wuye.points.entity.CommunityComment;
import com.wuye.points.entity.CommunityPost;
import com.wuye.points.entity.CommunityPostLike;
import com.wuye.points.entity.ContentReport;
import com.wuye.points.entity.SensitiveWord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 社区论坛 5 张表（之前缺失、现已补齐）的实体映射与约束测试。
 * 使用 H2 的 MySQL 兼容模式，验证表结构可创建、json 列可读写、
 * 唯一键（防重复点赞 / 防重复举报）生效。
 *
 * 注意：@DataJpaTest 会按 create-drop 为全部实体建表，因此本测试同时也是一次
 * 「全量实体映射是否能在 MySQL 兼容模式下建表」的校验。
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CommunityEntityMappingTest {

    @Autowired
    private CommunityPostRepository postRepository;
    @Autowired
    private CommunityCommentRepository commentRepository;
    @Autowired
    private CommunityPostLikeRepository likeRepository;
    @Autowired
    private ContentReportRepository reportRepository;
    @Autowired
    private SensitiveWordRepository sensitiveWordRepository;

    @Test
    void communityPost_jsonColumn_roundTrip() {
        CommunityPost post = new CommunityPost();
        post.setId("cp_1");
        post.setCommunityId("com_1");
        post.setPropertyCompanyId("pc_1");
        post.setAuthorId("res_1");
        post.setContent("今天天气真好");
        post.setImageUrls("[\"https://example.com/a.png\",\"https://example.com/b.png\"]");

        postRepository.saveAndFlush(post);

        CommunityPost loaded = postRepository.findById("cp_1").orElseThrow();
        assertEquals("今天天气真好", loaded.getContent());
        assertEquals("[\"https://example.com/a.png\",\"https://example.com/b.png\"]", loaded.getImageUrls());
    }

    @Test
    void communityComment_persist() {
        CommunityComment comment = new CommunityComment();
        comment.setId("cc_1");
        comment.setPostId("cp_1");
        comment.setAuthorId("res_2");
        comment.setContent("赞同");

        commentRepository.saveAndFlush(comment);

        assertTrue(commentRepository.findById("cc_1").isPresent());
    }

    @Test
    void postLike_uniqueConstraint_preventsDuplicate() {
        CommunityPostLike like1 = new CommunityPostLike();
        like1.setId("like_1");
        like1.setPostId("cp_x");
        like1.setResidentId("res_1");
        likeRepository.saveAndFlush(like1);

        CommunityPostLike duplicate = new CommunityPostLike();
        duplicate.setId("like_2");
        duplicate.setPostId("cp_x");
        duplicate.setResidentId("res_1");
        assertThrows(DataIntegrityViolationException.class, () -> likeRepository.saveAndFlush(duplicate));
    }

    @Test
    void contentReport_uniqueConstraint_preventsDuplicate() {
        ContentReport report1 = new ContentReport();
        report1.setId("crp_1");
        report1.setReporterId("res_1");
        report1.setTargetType("post");
        report1.setTargetId("cp_x");
        reportRepository.saveAndFlush(report1);

        ContentReport duplicate = new ContentReport();
        duplicate.setId("crp_2");
        duplicate.setReporterId("res_1");
        duplicate.setTargetType("post");
        duplicate.setTargetId("cp_x");
        assertThrows(DataIntegrityViolationException.class, () -> reportRepository.saveAndFlush(duplicate));
    }

    @Test
    void sensitiveWord_persistAndQuery() {
        SensitiveWord word = new SensitiveWord();
        word.setId("sw_1");
        word.setWord("敏感词示例");

        sensitiveWordRepository.saveAndFlush(word);

        assertTrue(sensitiveWordRepository.findById("sw_1").isPresent());
    }
}
