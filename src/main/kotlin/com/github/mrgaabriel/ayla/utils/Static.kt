package com.github.mrgaabriel.ayla.utils

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.gson.Gson
import com.google.gson.JsonParser

// Esta classe contém algumas instâncias de classes úteis que serão inicializadas juntamente com a JVM para economizar tempo de Garbage Clean
object Static {

    val YAML_MAPPER = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)).registerKotlinModule()
    val JSON_MAPPER = ObjectMapper(JsonFactory()).registerKotlinModule()

    val GSON = Gson()
    val JSON_PARSER = JsonParser()
}