package com.github.mrgaabriel.ayla.frontend

import kotlinx.css.*
import kotlinx.css.properties.LineHeight
import kotlinx.html.*
import kotlinx.html.stream.appendHTML

object MainPage {

    fun generate(): String {
        val css = CSSBuilder().apply {
            body {
                backgroundColor = Color.mediumPurple
            }

            h1 {
                fontFamily = "Whitney HTF,Helvetica Neue,Helvetica,Arial,sans-serif"
                fontSize = LinearDimension("64px")

                lineHeight = LineHeight("100px")
                left = LinearDimension("0")

                margin = "auto"
                marginTop = LinearDimension("-100px")
                top = LinearDimension("50%")

                width = LinearDimension("100%")

                position = Position.absolute

                textAlign = TextAlign.center

                color = Color.white
            }

            h2 {
                fontFamily = "roboto-light"
                fontSize = LinearDimension("24px")

                lineHeight = LineHeight("200px")
                left = LinearDimension("0")

                margin = "auto"
                marginTop = LinearDimension("-100px")
                top = LinearDimension("50%")

                width = LinearDimension("100%")

                position = Position.absolute

                textAlign = TextAlign.center

                color = Color.white
            }
        }

        val builder = StringBuilder()

        builder.appendHTML().html {
            head {
                link(rel = "icon", href = "https://mrgaabriel.website/files/ayla.png")
                title { + "Ayla" }

                style {
                    unsafe {
                        raw("""
                            @font-face {
                               font-family: roboto-light;
                               src: url(/assets/font/roboto.ttf);
                            }
                        """.trimIndent())
                    }
                    + css.toString()
                }
            }

            body {
                h1 {
                    + "Ayla"
                }

                h2 {
                    + "Somente mais um bot de terras tupiniquins para alegrar seu servidor!"
                }
            }
        }

        return builder.toString()
    }
}