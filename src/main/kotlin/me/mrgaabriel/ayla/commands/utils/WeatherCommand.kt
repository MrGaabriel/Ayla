package me.mrgaabriel.ayla.commands.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import me.mrgaabriel.ayla.utils.AylaUtils
import me.mrgaabriel.ayla.utils.Constants
import me.mrgaabriel.ayla.utils.ayla
import me.mrgaabriel.ayla.utils.commands.AbstractCommand
import me.mrgaabriel.ayla.utils.commands.CommandCategory
import me.mrgaabriel.ayla.utils.commands.CommandContext
import me.mrgaabriel.ayla.utils.commands.annotations.ArgumentType
import me.mrgaabriel.ayla.utils.commands.annotations.InjectArgument
import me.mrgaabriel.ayla.utils.commands.annotations.Subcommand
import me.mrgaabriel.ayla.utils.tag
import net.dv8tion.jda.core.EmbedBuilder
import java.net.URLEncoder
import java.time.OffsetDateTime

class WeatherCommand : AbstractCommand("weather", category = CommandCategory.UTILS) {

    @Subcommand
    fun weather(context: CommandContext, @InjectArgument(ArgumentType.ARGUMENT_LIST) city: String?) {
        if (city == null)
            return context.explain()

        val body = HttpRequest.get("https://api.openweathermap.org/data/2.5/weather?q=${URLEncoder.encode(city)}&APPID=${ayla.config.openWeatherMapKey}&units=metric")
                .acceptJson()
                .userAgent(Constants.USER_AGENT)
                .body()

        val payload = JsonParser().parse(body).obj

        val code = payload["cod"].string

        if (code == "404") {
            return context.sendMessage(context.getAsMention(true) + "Local não encontrado!")
        }

        val local = payload["name"].string
        val countryCode = payload["sys"].obj["country"].string

        val currentTemp = payload["main"].obj["temp"].double
        val minTemp = payload["main"].obj["temp_min"].double
        val maxTemp = payload["main"].obj["temp_max"].double

        val humidity = payload["main"].obj["humidity"].int

        val windSpeed = payload["wind"].obj["speed"].int
        val windDirection = payload["wind"].obj["deg"].int

        val pressure = payload["main"].obj["pressure"].int

        val longitude = payload["coord"].obj["lon"].double
        val latitude = payload["coord"].obj["lat"].double

        val builder = EmbedBuilder()

        builder.setColor(AylaUtils.randomColor())
        builder.setAuthor(context.user.tag, null, context.user.effectiveAvatarUrl)

        builder.setTitle(":tv: Previsão do tempo para $local, $countryCode")

        builder.addField(":globe_with_meridians: Informações geográficas", "**Latitude:** $latitude\n**Longitude:** $longitude", false)
        builder.addField(":information_source: Informações", "**Humidade do ar:** $humidity%\n**Pressão do ar:** $pressure kPA", true)
        builder.addField(":thermometer: Temperatura", "**Atual:** $currentTemp ºC\n**Mínima:** $minTemp ºC\n**Máxima:** $maxTemp ºC", true)
        builder.addField(":wind_blowing_face: Vento", "**Velocidade:** $windSpeed km/h\n**Direção:** ${windDirection}º", true)

        builder.setFooter("Powered by OpenWeatherMap (https://openweathermap.org/)", null)
        builder.setTimestamp(OffsetDateTime.now())

        context.sendMessage(builder.build(), context.getAsMention())
    }
}