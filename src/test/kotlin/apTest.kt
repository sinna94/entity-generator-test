import org.assertj.core.api.Assertions.assertThat
import org.example.App
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import javax.persistence.EntityManager

@SpringBootTest(classes = [App::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@AutoConfigureMockMvc
class apTest(
    private val em: EntityManager
) {

    @Test
    fun ap() {
        val metamodel = em.metamodel
        val entities = metamodel.entities
        assertThat(entities).hasSize(4)
            .extracting("name")
            .contains("E1","E2","E3", "Dummy")
    }
}