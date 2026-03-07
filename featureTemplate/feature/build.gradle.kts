import com.hand.log.setNamespace

plugins {
	alias(libs.plugins.hmm.android.feature)
}

android {
	setNamespace("{{lowerCamelClassName}}")
}
