package org.example.entity

import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Dummy(
    @Id
    var id: Long = 0L,
    var name: String
)
