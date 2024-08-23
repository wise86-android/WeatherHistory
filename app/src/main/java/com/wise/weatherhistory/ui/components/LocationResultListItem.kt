package com.wise.weatherhistory.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.wise.weatherhistory.R
import com.wise.weatherhistory.model.Location

@Composable
fun LocationResultListItem(location: Location, onSelect:(Location)->Unit){
    TextButton(onClick = { onSelect(location) },modifier = Modifier.padding(
        start = 8.dp,
        top = 4.dp,
        end = 8.dp,
        bottom = 4.dp
    )) {
        Column {
            Text(text = stringResource(id = R.string.search_bar_result_title,location.name,location.elevation),
                    style = MaterialTheme.typography.bodyMedium
            )
            Text(text = if(location.region!==null){"${location.region},  ${location.country}"}else{location.country},
                style = MaterialTheme.typography.bodySmall)
        }

    }
}


class PreviewLocationResultListItemProvider: PreviewParameterProvider<Location> {
    override val values = sequenceOf(
        Location(1.0f,2.0f,1300.0f,"name","country","region"),
        Location(1.0f,2.0f,2100.0f,"name","country")
    )
}
@Preview(showBackground = true)
@Composable
fun PreviewLocationResultListItem(@PreviewParameter(PreviewLocationResultListItemProvider::class) location: Location){
    LocationResultListItem(location) {}
}