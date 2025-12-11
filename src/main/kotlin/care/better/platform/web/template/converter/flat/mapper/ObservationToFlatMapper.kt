package care.better.platform.web.template.converter.flat.mapper

import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.value.ValueConverter
import org.openehr.rm.composition.Observation

internal object ObservationToFlatMapper : EntryToFlatMapper<Observation>() {
    override fun map(
        webTemplateNode: WebTemplateNode,
        valueConverter: ValueConverter,
        rmObject: Observation,
        webTemplatePath: String,
        flatConversionContext: FlatMappingContext
    ) {
        super.map(webTemplateNode, valueConverter, rmObject, webTemplatePath, flatConversionContext)

        rmObject.data?.origin?.let { origin ->
            DvDateTimeToFlatMapper.map(
                webTemplateNode, valueConverter, origin, "$webTemplatePath/history_origin", flatConversionContext
            )
        }
    }


    override fun mapFormatted(
        webTemplateNode: WebTemplateNode,
        valueConverter: ValueConverter,
        rmObject: Observation,
        webTemplatePath: String,
        formattedFlatConversionContext: FormattedFlatMappingContext
    ) {
        super.mapFormatted(webTemplateNode, valueConverter, rmObject, webTemplatePath, formattedFlatConversionContext)

        rmObject.data?.origin?.let { origin ->
            DvDateTimeToFlatMapper.mapFormatted(
                webTemplateNode, valueConverter, origin, "$webTemplatePath/history_origin", formattedFlatConversionContext
            )
        }
    }
}