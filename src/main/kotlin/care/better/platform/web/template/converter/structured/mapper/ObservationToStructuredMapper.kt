package care.better.platform.web.template.converter.structured.mapper

import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.mapper.putSingletonAsArray
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.rm.composition.Observation

internal object ObservationToStructuredMapper : EntryToStructuredMapper<Observation>() {

    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: Observation): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            map(webTemplateNode, valueConverter, rmObject, this)

            rmObject.data?.origin?.let { origin ->
                this.putSingletonAsArray("history_origin") {
                    DvDateTimeToStructuredMapper.map(webTemplateNode, valueConverter, origin)
                }
            }

            this
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: Observation): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            mapFormatted(webTemplateNode, valueConverter, rmObject, this)

            rmObject.data?.origin?.let { origin ->
                this.putSingletonAsArray("history_origin") {
                    DvDateTimeToStructuredMapper.map(webTemplateNode, valueConverter, origin)
                }
            }

            this
        }
}
