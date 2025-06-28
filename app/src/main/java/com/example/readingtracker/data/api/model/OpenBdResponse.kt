package com.example.readingtracker.data.api.model

import com.google.gson.annotations.SerializedName

data class OpenBdResponse(
    @SerializedName("summary")
    val summary: OpenBdSummary?,
    @SerializedName("onix")
    val onix: OpenBdOnix?
)

data class OpenBdSummary(
    @SerializedName("isbn")
    val isbn: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("author")
    val author: String?,
    @SerializedName("publisher")
    val publisher: String?,
    @SerializedName("pubdate")
    val pubdate: String?,
    @SerializedName("cover")
    val cover: String?
)

data class OpenBdOnix(
    @SerializedName("DescriptiveDetail")
    val descriptiveDetail: DescriptiveDetail?
)

data class DescriptiveDetail(
    @SerializedName("TitleDetail")
    val titleDetail: TitleDetail?,
    @SerializedName("Contributor")
    val contributor: List<Contributor>?
)

data class TitleDetail(
    @SerializedName("TitleElement")
    val titleElement: TitleElement?
)

data class TitleElement(
    @SerializedName("TitleText")
    val titleText: TitleText?
)

data class TitleText(
    @SerializedName("content")
    val content: String?
)

data class Contributor(
    @SerializedName("PersonName")
    val personName: PersonName?
)

data class PersonName(
    @SerializedName("content")
    val content: String?
)