package me.mrgaabriel.ayla.utils.youtube

// TODOS os direitos reservados a MrPowerGamerBR
// https://github.com/MrPowerGamerBR

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.obj
import com.google.gson.Gson
import com.google.gson.JsonParser
import me.mrgaabriel.ayla.utils.ayla
import org.apache.commons.lang3.StringUtils
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

class TemmieYouTube {

    /**
     * Pesquisa algo no YouTube
     *
     * @return O resultado da pesquisa
     */
    fun searchOnYouTube(searchQuery: String): SearchResponse {
        val key = ayla.config.youtubeApiKey
        val params = HashMap<String, Any>()
        params["part"] = "snippet"
        params["q"] = searchQuery
        params["key"] = key
        val req = HttpRequest.get("https://www.googleapis.com/youtube/v3/search?" + buildQuery(params))

        val body = req.body()

        val searchJson = JsonParser().parse(body).obj
        return Gson().fromJson(body, SearchResponse::class.java)
    }

    private fun buildQuery(params: Map<String, Any>): String {
        val query = arrayOfNulls<String>(params.size)
        var index = 0
        for (key in params.keys) {
            var `val` = (if (params[key] != null) params[key] else "").toString()
            try {
                `val` = URLEncoder.encode(`val`, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
            }

            query[index++] = key + "=" + `val`
        }

        return StringUtils.join(query, "&")
    }
}

    data class SearchResponse(
            val kind: String,
            val etag: String,
            val nextPageToken: String,
            val regionCode: String,
            val pageInfo: PageInfo,
            val items: List<YouTubeItem>)

    data class YouTubeItem(
            val kind: String,
            val etag: String,
            val id: YouTubeId,
            val snippet: Snippet)

    data class Thumbnails(
            val default: ThumbnailInfo,
            val medium: ThumbnailInfo,
            val high: ThumbnailInfo)

    data class Snippet(
            val publishedAt: String,
            val channelId: String,
            val title: String,
            val description: String,
            val thumbnails: Thumbnails,
            val channelTitle: String,
            val liveBroadcastContent: String)

    data class PageInfo(
            val totalResults: Int,
            val resultsPerPage: Int)

    data class ThumbnailInfo(
            val url: String,
            val width: Int,
            val height: Int)

    data class YouTubeId(
            val kind: String,
            val videoId: String)
