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

import care.better.platform.template.AmNode;
import care.better.platform.template.AmUtils;
import com.google.common.collect.ImmutableSet;
import com.marand.thinkehr.web.WebTemplateBuilderContext;
import com.marand.thinkehr.web.WebTemplateConstants;
import com.marand.thinkehr.web.build.WebTemplateInputType;
import com.marand.thinkehr.web.build.WebTemplateNode;
import com.marand.thinkehr.web.build.WebTemplateUtils;
import com.marand.thinkehr.web.build.input.range.WebTemplateDecimalRange;
import com.marand.thinkehr.web.build.input.range.WebTemplateValidationIntegerRange;
import org.openehr.am.aom.CBoolean;
import org.openehr.am.aom.CInteger;
import org.openehr.am.aom.CReal;
import org.openehr.rm.datatypes.DvProportion;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Bostjan Lah
 */
public class ProportionWebTemplateInputBuilder implements WebTemplateInputBuilder<Object> {
    private static final ImmutableSet<Integer> SINGLE_INPUT_TYPES =
            ImmutableSet.of(WebTemplateProportionType.PERCENT.ordinal(), WebTemplateProportionType.UNITARY.ordinal());
    private static final ImmutableSet<Integer> INTEGRAL_TYPES =
            ImmutableSet.of(WebTemplateProportionType.FRACTION.ordinal(), WebTemplateProportionType.INTEGER_FRACTION.ordinal());

    @Override
    public WebTemplateInput build(AmNode amNode, @Nullable Object ignored, WebTemplateBuilderContext context) {
        if (amNode != null) {
            CInteger type = AmUtils.getPrimitiveItem(amNode, CInteger.class, "type");
            CInteger precision = AmUtils.getPrimitiveItem(amNode, CInteger.class, "precision");
            CBoolean isIntegral = AmUtils.getPrimitiveItem(amNode, CBoolean.class, "is_integral");
            DvProportion defaultValue = WebTemplateUtils.getDefaultValue(amNode, DvProportion.class);

            WebTemplateInput input = new WebTemplateInput(WebTemplateInputType.PROPORTION);
            if (type != null && isSingleInput(type)) {
                WebTemplateInput numerator = createInput(amNode, WebTemplateConstants.NUMERATOR, type, precision, isIntegral,
                                                         defaultValue == null ? null : defaultValue.getNumerator());
                if (type.getList().contains(WebTemplateProportionType.PERCENT.ordinal())) {
                    WebTemplateCodedValue numeratorCodedValue = new WebTemplateCodedValue("%", "%");
                    numeratorCodedValue.setValidation(numerator.getValidation());
                    input.getList().add(numeratorCodedValue);
                } else {
                    WebTemplateCodedValue numeratorCodedValue = new WebTemplateCodedValue("", "");
                    numeratorCodedValue.setValidation(numerator.getValidation());
                    input.getList().add(numeratorCodedValue);
                }
                if (defaultValue != null) {
                    input.setDefaultValue(String.valueOf(defaultValue.getNumerator()));
                }
            } else {
                WebTemplateInput numerator = createInput(amNode, WebTemplateConstants.NUMERATOR, type, precision, isIntegral,
                                                         defaultValue == null ? null : defaultValue.getNumerator());
                WebTemplateInput denominator = createInput(amNode, WebTemplateConstants.DENOMINATOR, type, precision, isIntegral,
                                                           defaultValue == null ? null : defaultValue.getDenominator());
                WebTemplateCodedValue numeratorCodedValue = new WebTemplateCodedValue(WebTemplateConstants.NUMERATOR, "");
                numeratorCodedValue.setValidation(numerator.getValidation());
                input.getList().add(numeratorCodedValue);
                WebTemplateCodedValue denominatorCodedValue = new WebTemplateCodedValue(WebTemplateConstants.DENOMINATOR, "");
                denominatorCodedValue.setValidation(denominator.getValidation());
                input.getList().add(denominatorCodedValue);

                if (defaultValue != null) {
                    input.setDefaultValue(String.valueOf(defaultValue.getNumerator()) + '/' + String.valueOf(defaultValue.getDenominator()));
                }
            }

            return input;
        } else {
            return new WebTemplateInput(WebTemplateInputType.PROPORTION);
        }
    }

    @Override
    public void build(WebTemplateNode node, WebTemplateBuilderContext context) {
        CInteger type = AmUtils.getPrimitiveItem(node.getAmNode(), CInteger.class, "type");
        CInteger precision = AmUtils.getPrimitiveItem(node.getAmNode(), CInteger.class, "precision");
        CBoolean isIntegral = AmUtils.getPrimitiveItem(node.getAmNode(), CBoolean.class, "is_integral");
        DvProportion defaultValue = WebTemplateUtils.getDefaultValue(node.getAmNode(), DvProportion.class);
        if (type != null && isSingleInput(type)) {
            node.getInputs().add(
                    createInput(node.getAmNode(),
                                WebTemplateConstants.NUMERATOR,
                                type,
                                precision,
                                isIntegral,
                                defaultValue == null ? null : defaultValue.getNumerator()));
            node.getInputs().add(createFixedDenominator(type, precision, isIntegral));
        } else {
            node.getInputs().add(
                    createInput(node.getAmNode(),
                                WebTemplateConstants.NUMERATOR,
                                type,
                                precision,
                                isIntegral,
                                defaultValue == null ? null : defaultValue.getNumerator()));
            node.getInputs().add(
                    createInput(node.getAmNode(),
                                WebTemplateConstants.DENOMINATOR,
                                type,
                                precision,
                                isIntegral,
                                defaultValue == null ? null : defaultValue.getDenominator()));
        }

        Stream<WebTemplateProportionType> stream = Arrays.stream(WebTemplateProportionType.values());
        if (type != null) {
            stream = stream.filter(pt -> type.getList().contains(pt.ordinal()));
        }
        node.getProportionTypes().addAll(stream.map(Enum::name).map(String::toLowerCase).collect(Collectors.toSet()));
    }

    private WebTemplateInput createFixedDenominator(CInteger type, CInteger precision, CBoolean integral) {
        boolean isIntegral = isIntegral(type, precision, integral);
        WebTemplateInput input = new WebTemplateInput(isIntegral ? WebTemplateInputType.INTEGER : WebTemplateInputType.DECIMAL, WebTemplateConstants.DENOMINATOR);
        if (type.getList().size() == 2) { // percent and unitary
            WebTemplateCodedValue unitary = new WebTemplateCodedValue("1", "1");
            unitary.setValidation(new WebTemplateValidation());
            if (isIntegral) {
                unitary.getValidation().setRange(new WebTemplateValidationIntegerRange(1, 1));
            } else {
                unitary.getValidation().setRange(new WebTemplateDecimalRange(1.0f, ">=", 1.0f, "<="));
            }
            input.getList().add(unitary);

            WebTemplateCodedValue percent = new WebTemplateCodedValue("100", "100");
            percent.setValidation(new WebTemplateValidation());
            if (isIntegral) {
                percent.getValidation().setRange(new WebTemplateValidationIntegerRange(100, 100));
            } else {
                percent.getValidation().setRange(new WebTemplateDecimalRange(100.0f, ">=", 100.0f, "<="));
            }
            input.getList().add(percent);
        } else if (!type.getList().isEmpty() && type.getList().get(0).equals(WebTemplateProportionType.UNITARY.ordinal())) {
            input.setValidation(new WebTemplateValidation());
            if (isIntegral) {
                input.getValidation().setRange(new WebTemplateValidationIntegerRange(1, 1));
            } else {
                input.getValidation().setRange(new WebTemplateDecimalRange(1.0f, ">=", 1.0f, "<="));

            }
        } else {
            input.setValidation(new WebTemplateValidation());
            if (isIntegral) {
                input.getValidation().setRange(new WebTemplateValidationIntegerRange(100, 100));
            } else {
                input.getValidation().setRange(new WebTemplateDecimalRange(100.0f, ">=", 100.0f, "<="));
            }
        }
        return input;
    }

    private WebTemplateInput createInput(AmNode amNode, String suffix, CInteger type, CInteger precision, CBoolean integral, Float defaultValue) {
        CReal cReal = AmUtils.getPrimitiveItem(amNode, CReal.class, suffix);

        WebTemplateInput input;
        if (isIntegral(type, precision, integral)) {
            input = createIntegerInput(suffix, cReal, defaultValue == null ? null : Math.round(defaultValue));
        } else {
            input = createDecimalInput(suffix, cReal, defaultValue);
        }

        return input;
    }

    private WebTemplateInput createDecimalInput(String suffix, CReal cReal, Float defaultValue) {
        WebTemplateInput input = new WebTemplateInput(WebTemplateInputType.DECIMAL, suffix);
        if (cReal != null) {
            if (cReal.getRange() != null) {
                WebTemplateDecimalRange range = new WebTemplateDecimalRange(cReal.getRange());
                if (!range.isEmpty()) {
                    input.setValidation(new WebTemplateValidation());
                    input.getValidation().setRange(range);
                }
                input.setFixed(range.isFixed() || cReal.getList().size() == 1);
            }
            if (!cReal.getList().isEmpty()) {
                input.getList().addAll(cReal.getList().stream().map(WebTemplateCodedValue.TO_CODED_VALUE).collect(Collectors.toList()));
            }
            if (cReal.getAssumedValue() != null) {
                input.setDefaultValue(cReal.getAssumedValue());
            }
        }
        if (defaultValue != null) {
            input.setDefaultValue(defaultValue);
        }
        return input;
    }

    private WebTemplateInput createIntegerInput(String suffix, CReal cReal, Integer defaultValue) {
        WebTemplateInput input = new WebTemplateInput(WebTemplateInputType.INTEGER, suffix);
        if (cReal != null) {
            if (cReal.getRange() != null) {
                Integer min = cReal.getRange().getLower() != null ? Math.round(cReal.getRange().getLower()) : null;
                Integer max = cReal.getRange().getUpper() != null ? Math.round(cReal.getRange().getUpper()) : null;
                WebTemplateValidationIntegerRange range = new WebTemplateValidationIntegerRange(min, max);
                if (!range.isEmpty()) {
                    input.setValidation(new WebTemplateValidation());
                    input.getValidation().setRange(range);
                }
                input.setFixed(range.isFixed() || cReal.getList().size() == 1);
            }
            if (!cReal.getList().isEmpty()) {
                input.getList().addAll(cReal.getList().stream().map(real -> {
                    String inputValue = String.valueOf(Math.round(real));
                    return new WebTemplateCodedValue(inputValue, inputValue);
                }).collect(Collectors.toList()));
            }
            if (cReal.getAssumedValue() != null) {
                input.setDefaultValue(Math.round(cReal.getAssumedValue()));
            }
        }
        if (defaultValue != null) {
            input.setDefaultValue(defaultValue);
        }
        return input;
    }

    private boolean isSingleInput(CInteger type) {
        return type.getList().stream().allMatch(SINGLE_INPUT_TYPES::contains);
    }

    private boolean isIntegral(CInteger type, CInteger precision, CBoolean cIntegral) {
        boolean integral;

        if (cIntegral != null && cIntegral.getTrueValid() && !cIntegral.getFalseValid()) {
            integral = true;

        } else if (precision != null && Integer.valueOf(0).equals(AmUtils.getMax(precision.getRange()))) {
            integral = true;

        } else if (type != null) {
            integral = type.getList().stream().allMatch(INTEGRAL_TYPES::contains);

        } else {
            integral = false;
        }

        return integral;
    }
}
