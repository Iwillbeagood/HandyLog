package com.hand.log.data.network.mapper

import com.hand.log.data.network.model.SampleData
import com.hand.log.domain.model.Sample

internal fun SampleData.toSample(): Sample {
	return Sample(
		id = id,
		name = name,
	)
}
