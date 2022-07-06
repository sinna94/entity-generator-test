package org.example

import com.fasterxml.jackson.databind.ObjectMapper
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.persistence.*
import javax.tools.Diagnostic
import javax.tools.StandardLocation

@SupportedAnnotationTypes("org.example.GenerateEntity")
class GenerateEntityProcessor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {

        processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "GENERATE ENTITIES")

        val filer = processingEnv.filer
        val resource = filer.getResource(StandardLocation.SOURCE_PATH, "", "generate-entity.json")
        val inputStream = resource.openInputStream()

        inputStream.use {
            processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, it.toString())
            val mapper = ObjectMapper()
            val entities = mapper.readValue<List<EntityProp>>(
                it,
                mapper.typeFactory.constructCollectionType(List::class.java, EntityProp::class.java)
            )
            entities.forEach { entityProp ->
                writeEntityFile(entityProp.name)
                processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "GENERATE ENTITY: ${entityProp.name}")
            }
        }

        return true
    }

    private fun writeEntityFile(className: String) {
        val packageName = "org.example.entity"
        val generatedDirectory = (processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
            ?.let { File(it, "$className.kt") }
            ?: throw IllegalArgumentException("No output directory"))

        val nullable = true

        val builder = TypeSpec.classBuilder(className)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("id", Long::class)
                    .build()
            )
            .addProperty(
                PropertySpec.builder("id", Long::class)
                    .initializer("id", 0L)
                    .addAnnotation(Id::class)
                    .addAnnotation(
                        AnnotationSpec.builder(GeneratedValue::class)
                            .addMember("strategy = %T.AUTO", GenerationType::class)
                            .build()
                    )
                    .addAnnotation(AnnotationSpec.builder(Column::class).addMember("nullable = %L", nullable).build())
                    .mutable()
                    .build()
            ).superclass(Any::class)
            .addAnnotation(Entity::class)
            .addAnnotation(Table::class)
        val typeSpec = builder.build()

        val fileSpec = FileSpec.builder(packageName, className)
            .addType(typeSpec)
            .build()

        fileSpec.writeTo(generatedDirectory)
//        val filer = processingEnv.filer
//        fileSpec.writeTo(filer)
//        processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "file : $packageName.$className.kt")
//        val entityFile = processingEnv.filer.createSourceFile("$className.kt")
//        PrintWriter(entityFile.openWriter()).use { writer ->
//            val entityContent = """
//                package $packageName
//
//                import org.hibernate.annotations.CreationTimestamp
//                import org.hibernate.annotations.UpdateTimestamp
//                import org.springframework.data.annotation.CreatedBy
//                import org.springframework.data.annotation.LastModifiedBy
//                import org.springframework.data.jpa.domain.support.AuditingEntityListener
//                import java.sql.Timestamp
//                import javax.persistence.*
//
//                @Entity
//                @EntityListeners(AuditingEntityListener::class)
//                @Table
//                data class $className(
//                    @Id
//                    @GeneratedValue(strategy = GenerationType.AUTO)
//                    @Column(nullable = false)
//                    override val id: Long = 0L,
//                ) : IdEntity<Long>
//
//            """.trimIndent()
//            writer.println(entityContent)
//        }
    }
}
