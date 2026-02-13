package com.zc.bakamitai.compose.features.home.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class TodayScheduleDTO(

	@SerialName("schedule")
	val schedule: List<ScheduleDTO>,

	@SerialName("tz")
	val tz: String
)

@Serializable
data class ScheduleDTO(

	@SerialName("aired")
	val aired: Boolean,

	@SerialName("image_url")
	val imageUrl: String,

	@SerialName("page")
	val page: String,

	@SerialName("time")
	val time: String,

	@SerialName("title")
	val title: String
)
