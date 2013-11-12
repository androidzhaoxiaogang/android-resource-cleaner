
package xiaogang.src;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

class ARC extends JFrame {
    private static final String VERSION_STR = "3.0 beta";
    private static final long serialVersionUID = -1600532762007579488L;

    private JMenu mFileMenu;
    private JMenu mActionMenu;
    private JMenu mHelpMenu;

    private JMenuItem mOpenFileMenuItem;
    private JMenuItem mDeleteMenuItem;
    private JMenuItem mDeleteAllMenuItem;
    private JMenuItem mExitMenuItem;
    private JMenuItem mHelpMenuItem;

    private JButton mJButton;
    private JMenuBar mJMenuBar1;
    private JList mUnusedResList;
    private JSeparator mJSeparator1, mJSeparator2;

    private ResProbe mCleanRes;

    private String mSPath;

    private boolean running = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ARC inst = new ARC();
                inst.setLocationRelativeTo(null);
                inst.setVisible(true);
            }
        });
    }

    public ARC() {
        super();
        setupGUI();
    }

    private void setupGUI() {
        try {
            BorderLayout thisLayout = new BorderLayout();
            getContentPane().setLayout(thisLayout);

            mUnusedResList = new JList();
            mUnusedResList.setListData(new String[] {
                    "请打开Android工程根目录"
            });
            mUnusedResList.setLayoutOrientation(JList.VERTICAL);
            JScrollPane listScroller = new JScrollPane(mUnusedResList);
            getContentPane().add(listScroller, BorderLayout.CENTER);

            mJButton = new JButton("开始");
            mJButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    if (running) {
                        mCleanRes.setbCancel(true);
                        mJButton.setText("开始");
                        running = false;
                        return;
                    }
                    if (mSPath == null && mSPath.length() <= 0) {
                        JOptionPane.showMessageDialog(ARC.this, "选择目录", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }

                    running = true;
                    mJButton.setText("取消");

                    new Thread() {

                        @Override
                        public void run() {
                            mCleanRes.run(ARC.this);
                        }
                    }.start();
                }
            });
            getContentPane().add(mJButton, BorderLayout.SOUTH);

            setSize(400, 300);
            {
                mJMenuBar1 = new JMenuBar();
                setJMenuBar(mJMenuBar1);
                {
                    mFileMenu = new JMenu();
                    mJMenuBar1.add(mFileMenu);
                    mFileMenu.setText("文件");
                    {
                        mOpenFileMenuItem = new JMenuItem();
                        mFileMenu.add(mOpenFileMenuItem);
                        mOpenFileMenuItem.setText("打开");
                        mOpenFileMenuItem.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if (e.getActionCommand().equals("打开")) {
                                    JFileChooser jFileChooser = new JFileChooser();
                                    jFileChooser
                                    .setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                                    int returnVal = jFileChooser.showOpenDialog(ARC.this);
                                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                                        mSPath = jFileChooser.getSelectedFile().getAbsolutePath();
                                        mCleanRes = new ResProbe(mSPath);
                                        mUnusedResList.setListData(new String[] {
                                                "工程目录: "+mSPath
                                        });
                                        mUnusedResList.invalidate();
                                    }
                                }
                            }
                        });
                    }
                    {
                        mJSeparator1 = new JSeparator();
                        mFileMenu.add(mJSeparator1);
                    }
                    {
                        mExitMenuItem = new JMenuItem();
                        mFileMenu.add(mExitMenuItem);
                        mExitMenuItem.setText("退出");
                        mExitMenuItem.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent arg0) {
                                ARC.this.dispose();
                            }
                        });
                    }
                }

                mActionMenu = new JMenu();
                mJMenuBar1.add(mActionMenu);
                mActionMenu.setText("操作");

                mDeleteMenuItem = new JMenuItem();
                mActionMenu.add(mDeleteMenuItem);
                mDeleteMenuItem.setText("删除");
                mDeleteMenuItem.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        mCleanRes.delete(mUnusedResList.getSelectedIndices());
                        mUnusedResList.setListData(mCleanRes.getResult());
                        mUnusedResList.invalidate();
                    }

                });
                mJSeparator2 = new JSeparator();
                mActionMenu.add(mJSeparator2);
                mDeleteAllMenuItem = new JMenuItem();
                mActionMenu.add(mDeleteAllMenuItem);
                mDeleteAllMenuItem.setText("删除所有");
                mDeleteAllMenuItem.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        mCleanRes.deleteAll();
                        mUnusedResList.setListData(mCleanRes.getResult());
                        mUnusedResList.invalidate();
                    }
                });

                mHelpMenu = new JMenu();
                mJMenuBar1.add(mHelpMenu);
                mHelpMenu.setText("帮助");

                mHelpMenuItem = new JMenuItem();
                mHelpMenu.add(mHelpMenuItem);
                mHelpMenuItem.setText("帮助");
                mHelpMenuItem.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        JOptionPane.showMessageDialog(ARC.this, "Version v" + VERSION_STR
                                + "\n赵小刚,  QQ: 286505491", "ARC",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            }
            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setResult(List<ResSet> list) {
        running = false;
        mJButton.setText("开始");
        String[] fileList = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            fileList[i] = list.get(i).toString();
        }
        if (fileList.length == 0) {
            fileList = new String[1];
            fileList[0] = "No result";
        }
        mUnusedResList.setListData(fileList);
        mUnusedResList.invalidate();
    }

    public void setProgress(File file) {
        String[] pathList = new String[3];
        pathList[0] = "正在处理... : " + mSPath;
        pathList[1] = file.getParent().replace(mSPath, "");
        pathList[2] = file.getName();
        mUnusedResList.setListData(pathList);
        mUnusedResList.invalidate();
    }
}
