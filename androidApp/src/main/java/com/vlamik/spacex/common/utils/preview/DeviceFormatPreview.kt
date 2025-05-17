package com.vlamik.spacex.common.utils.preview

import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "phone",
    device = "spec:width=360dp,height=640dp,dpi=480",
    showSystemUi = true
)
@Preview(
    name = "landscape",
    device = "spec:width=640dp,height=360dp,dpi=480",
    showSystemUi = true
)
@Preview(
    name = "foldable",
    device = "spec:width=673dp,height=841dp,dpi=480",
    showSystemUi = true
)
@Preview(
    name = "tablet",
    device = "spec:width=1280dp,height=800dp,dpi=480",
    showSystemUi = true
)

annotation class DeviceFormatPreview
