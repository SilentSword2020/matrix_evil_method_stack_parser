package com.matrix.parser;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.matrix.parser.MethodIssueParser.ParserResult;
import com.matrix.parser.model.GBC;
import com.matrix.parser.util.Util;

/**
 * 界面
 * 
 * @author yangqh
 *
 */
public class MatrixParser {

	//
	private JFrame mainFrame;
	private JLabel containerLabel;

	//
	private JButton methodMappingChooser;
	private JLabel filePathLabel;
	private JTextArea issueArea;
	private JTextArea resultArea;

	//
	private MethodIssueParser methodIssueParser;

	public static void main(String[] args) {
		new MatrixParser().show();
	}

	public MatrixParser() {
		initParser();
		initUI();
	}

	/**
	 * 初始化解析器
	 */
	private void initParser() {
		methodIssueParser = new MethodIssueParser(new MethodIssueParser.Listener() {

			@Override
			public void onResult(ParserResult result) {
				// 显示解析结果
				if (resultArea != null) {
					String content = String.format(Constant.test_matrix_result, result.processName, result.scene,
							result.costTime, result.costStackKey, result.stackDetail, result.result);
					resultArea.setText(content);
				}
			}

			@Override
			public void onError(CharSequence msg) {
				// 显示错误提示
				JOptionPane.showMessageDialog(mainFrame, msg, "", JOptionPane.WARNING_MESSAGE);
			}
		});
	}

	/**
	 * 清理数据，退出界面
	 */
	private void clearAndExit() {
		if (methodIssueParser != null) {
			methodIssueParser.clear();
			methodIssueParser = null;
		}
		System.exit(0);
	}

	/**
	 * 显示界面
	 */
	private void show() {
		if (mainFrame != null) {
			mainFrame.setVisible(true);
		}
	}

	/**
	 * 初始化ui
	 */
	private void initUI() {
		mainFrame = new JFrame(Constant.TITLE);
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				// 点击关闭按钮
				clearAndExit();
			}

		});
		mainFrame.setBounds(200, 200, 500, 800);

		initComponents();

		mainFrame.setVisible(true);
	}

	/**
	 * 初始化子控件
	 */
	private void initComponents() {
		// 容器
		containerLabel = new JLabel("", JLabel.CENTER);
		containerLabel.setLayout(new GridBagLayout());
		mainFrame.add(containerLabel);

		int row = 2;
		// methodMapping.txt文件
		JLabel methodMappingLabel = new JLabel(Constant.FILE_METHOD_MAPPING, JLabel.CENTER);
		containerLabel.add(methodMappingLabel, new GBC(0, row, 1, 1));
		// 选择文件
		methodMappingChooser = new JButton(Constant.LABEL_SELECT_FILE);
		methodMappingChooser.setBounds(0, 0, 50, 64);
		methodMappingChooser.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				selectFile();
			}
		});
		containerLabel.add(methodMappingChooser, new GBC(0, ++row, 1, 1));

		// 文件路径
		filePathLabel = new JLabel("", JLabel.CENTER);
		containerLabel.add(filePathLabel, new GBC(0, ++row, 1, 1).setFill(GBC.BOTH));

		// 日志内容标题
		JLabel issueLabel = new JLabel(Constant.LABEL_ISSUE_CONTENT, JLabel.CENTER);
		containerLabel.add(issueLabel, new GBC(0, ++row, 1, 1));

		// 日志内容
		issueArea = new JTextArea();
		JScrollPane jScrollPane = new JScrollPane();
		jScrollPane.setViewportView(issueArea);
		containerLabel.add(jScrollPane, new GBC(0, ++row, 1, 1).setWeight(1, 0.5f).setFill(GBC.BOTH));

		// 开始解析按钮
		JButton parserBtn = new JButton(Constant.LABEL_START_PARSER);
		parserBtn.setBounds(0, 0, 50, 64);
		parserBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				startParser();
			}

		});
		containerLabel.add(parserBtn, new GBC(0, ++row, 1, 1));

		// 解析结果的标题
		JLabel resultLabel = new JLabel(Constant.LABEL_PARSER_RESULT, JLabel.CENTER);
		containerLabel.add(resultLabel, new GBC(0, ++row, 1, 1).setFill(GBC.BOTH));

		// 解析结果
		jScrollPane = new JScrollPane();
		resultArea = new JTextArea();
		jScrollPane.setViewportView(resultArea);
		containerLabel.add(jScrollPane, new GBC(0, ++row, 1, 1).setWeight(1, 1).setFill(GBC.BOTH));

	}

	/**
	 * 开始解析
	 */
	private void startParser() {
		formatIssueTxt();
		if (methodIssueParser != null && issueArea != null) {
			methodIssueParser.start(issueArea.getText());
		}

	}

	/**
	 * 格式化问题内容
	 */
	private void formatIssueTxt() {
		if (methodIssueParser != null && issueArea != null) {
			String result = methodIssueParser.getPrettyStr(issueArea.getText());
			if (!Util.isEmpty(result)) {
				issueArea.setText(result);
			}
		}
	}

	/**
	 * 选择文件
	 */
	private void selectFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.showDialog(new JLabel(), Constant.LABEL_SELECT_FILE);
		File file = fileChooser.getSelectedFile();
		if (file == null || file.isDirectory()) {
			return;
		}
		if (file.isFile()) {
			System.out.println(file.getAbsolutePath());
			if (filePathLabel != null) {
				filePathLabel.setText(file.getAbsolutePath());
			}
			if (methodIssueParser != null) {
				methodIssueParser.setMethodMappingFile(file);
			}

		}
	}

}