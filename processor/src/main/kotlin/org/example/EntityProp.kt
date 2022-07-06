package org.example

import com.fasterxml.jackson.annotation.JsonProperty

open class EntityProp(@JsonProperty("name") var name: String)