/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 */
package org.knime.dl.keras.core.layers.impl.core;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.dl.keras.core.layers.DLInvalidTensorSpecException;
import org.knime.dl.keras.core.layers.DLKerasAbstractUnaryLayer;
import org.knime.dl.keras.core.layers.DLLayerUtils;
import org.knime.dl.keras.core.layers.dialog.tuple.DLKerasTuple;
import org.knime.dl.keras.core.layers.dialog.tuple.DLKerasTuple.Constraint;
import org.knime.dl.keras.core.struct.param.Parameter;

/**
 * @author Marcel Wiedenmann, KNIME GmbH, Konstanz, Germany
 * @author Christian Dietz, KNIME GmbH, Konstanz, Germany
 */
public final class DLKerasReshapeLayer extends DLKerasAbstractUnaryLayer {

    @Parameter(label = "Target shape")
    private DLKerasTuple m_shape = new DLKerasTuple("1", 1, 1000, EnumSet.noneOf(Constraint.class), true);

    /**
     * Constructor
     */
    public DLKerasReshapeLayer() {
        super("keras.layers.Reshape", DLLayerUtils.ALL_DTYPES);
    }

    @Override
    public void validateParameters() throws InvalidSettingsException {
    }

    @Override
    protected void validateInputShape(Long[] inputShape) throws DLInvalidTensorSpecException {
        //TODO instead of only checking the sizes if both shapes are fully defined we could calculate the unknown dimension and still check
        Long[] target = m_shape.getTuple();
        if (DLLayerUtils.isShapeFullyDefined(target) && DLLayerUtils.isShapeFullyDefined(inputShape)) {
            Long inputNumElems = DLLayerUtils.numberOfElements(inputShape);
            Long targetNumElements = DLLayerUtils.numberOfElements(target);
            checkInputSpec(inputNumElems.equals(targetNumElements),
                "The total number of elements of the input " + "shape must match"
                    + " the total number of elments of the target shape. Input shape was: "
                    + shapeAndElements(inputShape) + "; Target shape was: " + shapeAndElements(target) + ".");
        }
    }

    private static String shapeAndElements(Long[] shape) {
        return Arrays.toString(shape) + ": " + DLLayerUtils.numberOfElements(shape) + " element/s";
    }

    @Override
    protected Long[] inferOutputShape(Long[] inputShape) {
        return m_shape.getTuple();
    }

    @Override
    protected void populateParameters(final List<String> positionalParams, final Map<String, String> namedParams) {
        namedParams.put("target_shape", m_shape.toPytonTuple());
    }
}
