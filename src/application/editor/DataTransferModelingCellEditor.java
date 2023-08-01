package application.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.util.EventObject;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.view.mxICellEditor;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;

import models.algebra.Expression;
import models.dataFlowModel.DataTransferModel;
import models.dataFlowModel.DataTransferChannelGenerator;
import models.dataFlowModel.PushPullAttribute;
import models.dataFlowModel.PushPullValue;
import models.visualModel.FormulaChannelGenerator;
import parser.Parser;
import parser.Parser.TokenStream;
import parser.exceptions.ExpectedRightBracket;

public class DataTransferModelingCellEditor  implements mxICellEditor {
	public int DEFAULT_MIN_WIDTH = 70;
	public int DEFAULT_MIN_HEIGHT = 30;
	public double DEFAULT_MINIMUM_EDITOR_SCALE = 1;

	protected double minimumEditorScale = DEFAULT_MINIMUM_EDITOR_SCALE;
	protected int minimumWidth = DEFAULT_MIN_WIDTH;
	protected int minimumHeight = DEFAULT_MIN_HEIGHT;

	private Object editingCell;
	private EventObject trigger;
	private JComboBox<String> comboBox;
	private mxGraphComponent graphComponent;
	private Editor editor;

	public DataTransferModelingCellEditor(mxGraphComponent graphComponent, Editor editor) {
		this.graphComponent = graphComponent;
		this.editor = editor;
	}

	@Override
	public Object getEditingCell() {
		return editingCell;
	}

	@Override
	public void startEditing(Object cell, EventObject evt) {
		if (editingCell != null) {
			stopEditing(true);
		}

		if (!graphComponent.getGraph().getModel().isEdge(cell)) {
			DataTransferModel model = editor.getModel();
			DataTransferChannelGenerator ch = (DataTransferChannelGenerator) model.getChannelGenerator((String) ((mxCell) cell).getValue());
			if (ch == null) {
				ch = (DataTransferChannelGenerator) model.getIOChannelGenerator((String) ((mxCell) cell).getValue());
				if(ch == null) {
					//resource
					return;
				}
			}

			if(ch instanceof FormulaChannelGenerator) {

				JPanel panel = new JPanel();
				JLabel label1 = new JLabel("Formula: ");
				JLabel label2 = new JLabel("Source: ");
				GridBagLayout layout = new GridBagLayout();
				panel.setLayout(layout);
				GridBagConstraints gbc = new GridBagConstraints();

				gbc.gridx = 0;
				gbc.gridy = 0;
				layout.setConstraints(label1, gbc);
				panel.add(label1);

				gbc.gridx = 1;
				gbc.gridy = 0;
				JTextField formulaText = new JTextField(((FormulaChannelGenerator) ch).getFormula(),15);
				layout.setConstraints(formulaText, gbc);
				panel.add(formulaText);

				gbc.gridx = 0;
				gbc.gridy = 1;
				layout.setConstraints(label2, gbc);
				panel.add(label2);

				gbc.gridx = 1;
				gbc.gridy = 1;
				JTextArea textArea = new JTextArea(ch.getSourceText(),7,15);
				textArea.setEditable(false);
				layout.setConstraints(textArea, gbc);
				panel.add(textArea);

				int r = JOptionPane.showConfirmDialog(
						null,				// owner window
						panel,				// message
						"Edit Formula Channel",			// window's title
						JOptionPane.OK_CANCEL_OPTION,	// option (button types)
						JOptionPane.QUESTION_MESSAGE);	// message type (icon types)
				if(r == JOptionPane.OK_OPTION) {
					TokenStream stream = new Parser.TokenStream();
					Parser parser = new Parser(stream);

					String formula = formulaText.getText();
					stream.addLine(formula.split(Parser.EQUALS)[1]);

					try {
						Expression exp = parser.parseTerm(stream, editor.getModel());
						((FormulaChannelGenerator) ch).setFormula(formula);
						((FormulaChannelGenerator) ch).setFormulaTerm(exp);
					} catch (ExpectedRightBracket e) {
						e.printStackTrace();
					}
				}
			}else {
				JPanel panel = new JPanel();
				JTextArea textArea = new JTextArea(ch.getSourceText(), 10, 20);
				panel.add(textArea);
				//			JEditorPane panel = new JEditorPane("text/plain", ch.toString());
				//			panel.setEditable(true);
				int ret = JOptionPane.showConfirmDialog(null, panel, "Channel Code", JOptionPane.OK_CANCEL_OPTION);
				if (ret == JOptionPane.OK_OPTION) {
					editor.setChannelCode(ch, textArea.getText());
				}
			}
			return;
		}

		mxCellState state = graphComponent.getGraph().getView().getState(cell);
		if (state != null && state.getLabel() != null && !state.getLabel().equals("")) {
			editingCell = cell;
			trigger = evt;

			double scale = Math.max(minimumEditorScale, graphComponent.getGraph().getView().getScale());
			Object value = graphComponent.getGraph().getModel().getValue(cell);
			if (value != null && value instanceof PushPullAttribute) {
				PushPullAttribute attr = (PushPullAttribute) value;
				comboBox = new JComboBox<>(attr.getOptionStrings());
				comboBox.setBorder(BorderFactory.createEmptyBorder());
				comboBox.setOpaque(false);
				comboBox.setBounds(getEditorBounds(state, scale));
				comboBox.setVisible(true);
				graphComponent.getGraphControl().add(comboBox, 0);
				comboBox.updateUI();
			}
		}
	}

	@Override
	public void stopEditing(boolean cancel) {
		if (editingCell != null) {
			comboBox.transferFocusUpCycle();
			Object cell = editingCell;
			editingCell = null;
			if (!cancel) {
				EventObject trig = trigger;
				trigger = null;
				Object value = graphComponent.getGraph().getModel().getValue(cell);
				if (value != null && value instanceof PushPullAttribute) {
					PushPullAttribute attr = (PushPullAttribute) value;
					List<PushPullValue> options = attr.getOptions();
					PushPullValue selected = null;
					for (PushPullValue option: options) {
						if (option.toString().equals(getCurrentValue())) {
							selected = option;
							break;
						}
					}
					if (selected != null) {
						options.remove(selected);
						options.add(0, selected);
					}
					graphComponent.labelChanged(cell, attr, trig);
				}
			} else {
				mxCellState state = graphComponent.getGraph().getView().getState(cell);
				graphComponent.redraw(state);
			}

			if (comboBox.getParent() != null) {
				comboBox.setVisible(false);
				comboBox.getParent().remove(comboBox);
			}

			graphComponent.requestFocusInWindow();
		}
	}

	public String getCurrentValue() {
		return (String) comboBox.getSelectedItem();
	}

	/**
	 * Returns the bounds to be used for the editor.
	 */
	public Rectangle getEditorBounds(mxCellState state, double scale) {
		mxIGraphModel model = state.getView().getGraph().getModel();
		Rectangle bounds = null;

		bounds = state.getLabelBounds().getRectangle();
		bounds.height += 10;

		// Applies the horizontal and vertical label positions
		if (model.isVertex(state.getCell())) {
			String horizontal = mxUtils.getString(state.getStyle(), mxConstants.STYLE_LABEL_POSITION, mxConstants.ALIGN_CENTER);

			if (horizontal.equals(mxConstants.ALIGN_LEFT)) {
				bounds.x -= state.getWidth();
			} else if (horizontal.equals(mxConstants.ALIGN_RIGHT)) {
				bounds.x += state.getWidth();
			}

			String vertical = mxUtils.getString(state.getStyle(),
					mxConstants.STYLE_VERTICAL_LABEL_POSITION,
					mxConstants.ALIGN_MIDDLE);

			if (vertical.equals(mxConstants.ALIGN_TOP)) {
				bounds.y -= state.getHeight();
			} else if (vertical.equals(mxConstants.ALIGN_BOTTOM)) {
				bounds.y += state.getHeight();
			}
		}

		bounds.setSize(
				(int) Math.max(bounds.getWidth(),
						Math.round(minimumWidth * scale)),
				(int) Math.max(bounds.getHeight(),
						Math.round(minimumHeight * scale)));

		return bounds;
	}

}
