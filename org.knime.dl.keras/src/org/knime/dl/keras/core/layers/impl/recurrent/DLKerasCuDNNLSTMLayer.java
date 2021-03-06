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
package org.knime.dl.keras.core.layers.impl.recurrent;

import java.util.List;
import java.util.Map;

import org.knime.dl.core.DLTensorSpec;
import org.knime.dl.keras.core.struct.param.Parameter;
import org.knime.dl.python.util.DLPythonUtils;

/**
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public final class DLKerasCuDNNLSTMLayer extends DLKerasAbstractFCRNNLayer {

    /**
     * 
     */
    private static final int STATE_TWO = 2;

    /**
     * 
     */
    private static final int STATE_ONE = 1;

    /**
     * 
     */
    private static final int NUM_HIDDEN_STATES = 2;

    @Parameter(label = "Input tensor", min = "0")
    private DLTensorSpec m_inputTensor = null;
    
    @Parameter(label = "First hidden state tensor", min = "1")
    private DLTensorSpec m_hiddenStateTensor1 = null;
    
    @Parameter(label = "Second hidden state tensor", min = "2")
    private DLTensorSpec m_hiddenStateTensor2 = null;
    
    @Parameter(label = "Units", min = "1", stepSize = "1")
    private int m_units = DEFAULT_UNITS;
    
    @Parameter(label = "Return sequences")
    private boolean m_returnSequences = false;

    @Parameter(label = "Return state")
    private boolean m_returnState = false;
    // TODO add parameter for stateful once we support stateful execution (and learning)
    
    @Parameter(label = "Unit forget bias", tab = "Initializers")
    private boolean m_forgetBias = true;
    
    /**
     * Constructor for {@link DLKerasCuDNNLSTMLayer}s.
     */
    public DLKerasCuDNNLSTMLayer() {
        super("keras.layers.CuDNNLSTM", NUM_HIDDEN_STATES);
    }
    
    @Override
    protected void populateParameters(List<String> positionalParams, Map<String, String> namedParams) {
        super.populateParameters(positionalParams, namedParams);
        namedParams.put("unit_forget_bias", DLPythonUtils.toPython(m_forgetBias));
    }
    
    @Override
    public DLTensorSpec getInputTensorSpec(int index) {
        if (index == 0) {
            return m_inputTensor;
        } else if (index == STATE_ONE) {
            return m_hiddenStateTensor1;
        } else if (index == STATE_TWO) {
            return m_hiddenStateTensor2;
        } else {
            throw new IllegalArgumentException("This layer has only 3 possible input ports.");
        }
    }
    
    @Override
    public void setInputTensorSpec(int index, DLTensorSpec inputTensorSpec) {
        if (index == 0) {
            m_inputTensor = inputTensorSpec;
        } else if (index == STATE_ONE) {
            m_hiddenStateTensor1 = inputTensorSpec;
        } else if (index == STATE_TWO) {
            m_hiddenStateTensor2 = inputTensorSpec;
        } else {
            throw new IllegalArgumentException("This layer has only 3 possible input ports.");
        }
    }
    
    @Override
    protected int getUnits() {
        return m_units;
    }

    @Override
    protected boolean returnState() {
        return m_returnState;
    }

    @Override
    protected boolean returnSequences() {
        return m_returnSequences;
    }

}
