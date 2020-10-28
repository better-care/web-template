/* Copyright 2021 Better Ltd (www.better.care)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.marand.thinkehr.web.build.input;

import care.better.openehr.rm.RmObject;
import com.google.common.base.Preconditions;
import com.marand.thinkehr.web.WebTemplateBuilderContext;
import com.marand.thinkehr.web.build.WebTemplateNode;
import org.openehr.am.aom.CBoolean;
import org.openehr.am.aom.CCodePhrase;
import org.openehr.am.aom.CCodeReference;
import org.openehr.am.aom.CDate;
import org.openehr.am.aom.CDateTime;
import org.openehr.am.aom.CDomainType;
import org.openehr.am.aom.CDuration;
import org.openehr.am.aom.CDvOrdinal;
import org.openehr.am.aom.CDvQuantity;
import org.openehr.am.aom.CInteger;
import org.openehr.am.aom.CPrimitive;
import org.openehr.am.aom.CReal;
import org.openehr.am.aom.CString;
import org.openehr.am.aom.CTime;
import org.openehr.rm.common.PartyIdentified;
import org.openehr.rm.common.PartyProxy;
import org.openehr.rm.datatypes.CodePhrase;
import org.openehr.rm.datatypes.DataValue;
import org.openehr.rm.datatypes.DvBoolean;
import org.openehr.rm.datatypes.DvCodedText;
import org.openehr.rm.datatypes.DvCount;
import org.openehr.rm.datatypes.DvDate;
import org.openehr.rm.datatypes.DvDateTime;
import org.openehr.rm.datatypes.DvDuration;
import org.openehr.rm.datatypes.DvEhrUri;
import org.openehr.rm.datatypes.DvIdentifier;
import org.openehr.rm.datatypes.DvMultimedia;
import org.openehr.rm.datatypes.DvParsable;
import org.openehr.rm.datatypes.DvProportion;
import org.openehr.rm.datatypes.DvText;
import org.openehr.rm.datatypes.DvTime;
import org.openehr.rm.datatypes.DvUri;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bostjan Lah
 */
public final class InputBuilders {
    private final Map<Class<? extends CDomainType>, WebTemplateInputBuilder<? extends CDomainType>> domainTypeBuilders = new HashMap<>();
    private final Map<Class<? extends CPrimitive>, WebTemplateInputBuilder<? extends CPrimitive>> primitiveBuilders = new HashMap<>();

    private final Map<Class<? extends RmObject>, WebTemplateInputBuilder<?>> dataValueBuilders = new HashMap<>();
    private final Map<String, WebTemplateInputBuilder<?>> buildersByType = new HashMap<>();

    private InputBuilders() {
        addDomainType(CCodePhrase.class, new CodePhraseWebTemplateInputBuilder());
        addDomainType(CCodeReference.class, new CodeReferenceWebTemplateInputBuilder());
        addDomainType(CDvOrdinal.class, new OrdinalWebTemplateInputBuilder());
        addDomainType(CDvQuantity.class, new QuantityWebTemplateInputBuilder());

        addPrimitive(CDateTime.class, new DateTimeWebTemplateInputBuilder());
        addPrimitive(CDate.class, new DateWebTemplateInputBuilder());
        addPrimitive(CTime.class, new TimeWebTemplateInputBuilder());
        addPrimitive(CDuration.class, new DurationWebTemplateInputBuilder());

        addPrimitive(CBoolean.class, new BooleanWebTemplateInputBuilder());
        addPrimitive(CInteger.class, new IntegerWebTemplateInputBuilder());
        addPrimitive(CReal.class, new DecimalWebTemplateInputBuilder());
        addPrimitive(CString.class, new StringWebTemplateInputBuilder());

        addDataValue(DvDateTime.class, new DateTimeWebTemplateInputBuilder());
        addDataValue(DvDate.class, new DateWebTemplateInputBuilder());
        addDataValue(DvTime.class, new TimeWebTemplateInputBuilder());
        addDataValue(DvDuration.class, new DurationWebTemplateInputBuilder());
        addDataValue(DvBoolean.class, new BooleanWebTemplateInputBuilder());
        addDataValue(DvProportion.class, new ProportionWebTemplateInputBuilder());
        addDataValue(DvCount.class, new IntegerWebTemplateInputBuilder());
        addDataValue(DvCodedText.class, new CodePhraseWebTemplateInputBuilder());
        addDataValue(DvMultimedia.class, new StringWebTemplateInputBuilder());
        addDataValue(DvText.class, new StringWebTemplateInputBuilder());
        addDataValue(DvIdentifier.class, new StringWebTemplateInputBuilder());
        addDataValue(DvParsable.class, new StringWebTemplateInputBuilder());
        addDataValue(DvUri.class, new StringWebTemplateInputBuilder());
        addDataValue(DvEhrUri.class, new StringWebTemplateInputBuilder());
        addDataValue(CodePhrase.class, new CodePhraseWebTemplateInputBuilder());

        addByType("DV_DATE_TIME", new DateTimeWebTemplateInputBuilder());
        addByType("DV_DATE", new DateWebTemplateInputBuilder());
        addByType("DV_TIME", new TimeWebTemplateInputBuilder());
        addByType("DV_DURATION", new DurationWebTemplateInputBuilder());
        addByType("DV_BOOLEAN", new BooleanWebTemplateInputBuilder());
        addByType("DV_PROPORTION", new ProportionWebTemplateInputBuilder());
        addByType("DV_COUNT", new IntegerWebTemplateInputBuilder());
        addByType("DV_CODED_TEXT", new CodePhraseWebTemplateInputBuilder());
        addByType("DV_MULTIMEDIA", new StringWebTemplateInputBuilder());
        addByType("DV_TEXT", new StringWebTemplateInputBuilder());
        addByType("DV_IDENTIFIER", new IdentifierWebTemplateInputBuilder());
        addByType("DV_PARSABLE", new StringWebTemplateInputBuilder());
        addByType("DV_URI", new StringWebTemplateInputBuilder());
        addByType("DV_EHR_URI", new StringWebTemplateInputBuilder());
        addByType("DV_QUANTITY", new QuantityWebTemplateInputBuilder());
        addByType("DV_ORDINAL", new OrdinalWebTemplateInputBuilder());
        addByType("DV_INTERVAL", new IntervalWebTemplateInputBuilder());
        addByType("DV_STATE", new CodePhraseWebTemplateInputBuilder());
        addByType("DV_PARSABLE", new ParsableWebTemplateInputBuilder());

        addByType("PARTY_PROXY", new PartyIdentifiedWebTemplateInputBuilder());
        addByType("PARTY_IDENTIFIED", new PartyIdentifiedWebTemplateInputBuilder());

        addByType("STRING", new StringWebTemplateInputBuilder());
//        addByType("CodePhrase", new CodePhraseWebTemplateInputBuilder());
    }

    private <T extends CDomainType> void addDomainType(Class<T> definedObjectClass, WebTemplateInputBuilder<T> webTemplateInputBuilder) {
        domainTypeBuilders.put(definedObjectClass, webTemplateInputBuilder);
    }

    private <T extends CPrimitive> void addPrimitive(Class<T> definedObjectClass, WebTemplateInputBuilder<T> webTemplateInputBuilder) {
        primitiveBuilders.put(definedObjectClass, webTemplateInputBuilder);
    }

    private <T extends RmObject> void addDataValue(Class<T> dataValueClass, WebTemplateInputBuilder<?> webTemplateInputBuilder) {
        dataValueBuilders.put(dataValueClass, webTemplateInputBuilder);
    }

    private void addByType(String rmType, WebTemplateInputBuilder<?> webTemplateInputBuilder) {
        buildersByType.put(rmType, webTemplateInputBuilder);
    }

    private WebTemplateInputBuilder<?> getBuilderByType(String rmType) {
        return buildersByType.get(rmType);
    }

    public static void buildByType(WebTemplateNode webTemplateNode, WebTemplateBuilderContext context) {
        WebTemplateInputBuilder<?> builder = Holder.INSTANCE.getBuilderByType(webTemplateNode.getRmType());
        Preconditions.checkNotNull(builder, "Unsupported type: %s", webTemplateNode.getRmType());
        builder.build(webTemplateNode, context);
    }

    public static boolean isRmTypeWithInputs(Class<? extends RmObject> rmClass) {
        return DataValue.class.isAssignableFrom(rmClass) || PartyProxy.class.equals(rmClass) || PartyIdentified.class.equals(rmClass);
    }

    private static final class Holder {
        public static final InputBuilders INSTANCE = new InputBuilders();
    }
}
