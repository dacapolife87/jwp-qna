package qna.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class AnswerRepositoryTest {

    @Autowired
    private AnswerRepository answerRepository;
    @Autowired
    private QuestionRepository questionRepository;

    Question savedQuestion;
    Answer answer;
    Answer savedAnswer;

    @BeforeEach
    void setUp() {
        savedQuestion = questionRepository.save(QuestionTest.Q1);

        answer = new Answer(UserTest.JAVAJIGI, savedQuestion, "답변1");
        savedAnswer = answerRepository.save(answer);
    }

    @DisplayName("DB에 저장된 Entity와 저장하기전 Entity가 동일한지 확인")
    @Test
    void insertTest() {
        Answer answer = new Answer(UserTest.JAVAJIGI, savedQuestion, "답변내용");
        Answer savedAnswer = answerRepository.save(answer);

        assertThat(savedAnswer).isEqualTo(answer);
    }

    @DisplayName("JPA Auditing 기능 테스트")
    @Test
    void entityAuditingTest() {
        LocalDateTime nowDt = LocalDateTime.now();

        assertAll(
                () -> assertThat(savedAnswer.getCreatedAt()).isNotNull(),
                () -> assertThat(savedAnswer.getCreatedAt()).isBefore(nowDt)
        );

    }

    @DisplayName("답변내용 업데이트 테스트")
    @Test
    void updateTest() {
        String contests = "업데이트 테스트";
        savedAnswer.setContents(contests);

        answerRepository.flush();

        Answer findAnswer = answerRepository.getOne(savedAnswer.getId());
        LocalDateTime nowDt = LocalDateTime.now();

        assertAll(
                () -> assertThat(findAnswer).isEqualTo(savedAnswer),
                () -> assertThat(findAnswer.getContents()).isEqualTo(contests),
                () -> assertThat(findAnswer.getUpdatedAt()).isBefore(nowDt)
        );

    }

    @DisplayName("삭제상태가 아닌 답변을 질문ID로 조회 테스트")
    @Test
    void findByQuestionIdAndDeletedFalse() {


        List<Answer> findAnswers = answerRepository.findByQuestionIdAndDeletedFalse(savedQuestion.getId());

        assertAll(
                () -> assertThat(findAnswers).hasSize(1),
                () -> assertThat(findAnswers).containsExactly(answer)
        );

    }

    @DisplayName("삭제상태인 답변을 질문ID로 조회했을때 조회안됨 테스트")
    @Test
    void notFoundByQuestionIdAndDeletedTrue() {
        savedAnswer.setDeleted(true);
        answerRepository.flush();

        List<Answer> findAnswers = answerRepository.findByQuestionIdAndDeletedFalse(savedQuestion.getId());

        assertAll(
                () -> assertThat(findAnswers).hasSize(0),
                () -> assertThat(findAnswers).isEmpty()
        );

    }

    @DisplayName("존재하지않는 질문ID로 조회 테스트")
    @Test
    void findByInvalidQuestionIdAndDeletedFalse() {
        Long questionId = 0L;

        List<Answer> findAnswers = answerRepository.findByQuestionIdAndDeletedFalse(questionId);

        assertAll(
                () -> assertThat(findAnswers).isEmpty(),
                () -> assertThat(findAnswers).hasSize(0)
        );

    }

    @DisplayName("삭제상태가 아닌 답변을 답변ID로 조회 테스트")
    @Test
    void findByIdAndDeletedFalse() {
        Optional<Answer> findAnswer = answerRepository.findByIdAndDeletedFalse(answer.getId());

        assertAll(
                () -> assertThat(findAnswer).contains(answer),
                () -> assertTrue(findAnswer.isPresent())
        );
    }

    @DisplayName("삭제상태인 답변을 답변ID로 조회했을때 조회안됨 테스트")
    @Test
    void notFoundByIdAndDeletedTrue() {
        savedAnswer.setDeleted(true);
        answerRepository.flush();

        Optional<Answer> findAnswer = answerRepository.findByIdAndDeletedFalse(answer.getId());

        assertAll(
                () -> assertThat(findAnswer).isEmpty(),
                () -> assertFalse(findAnswer.isPresent())
        );

    }

    @DisplayName("존재하지 않는 답변ID로 조회 테스트")
    @Test
    void findByInvalidIdAndDeletedFalse() {
        Long answerId = 0L;
        Optional<Answer> findAnswer = answerRepository.findByIdAndDeletedFalse(answerId);
        assertFalse(findAnswer.isPresent());
    }

}
