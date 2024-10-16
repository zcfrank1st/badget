package com.poorld.badget.activity;

import static com.poorld.badget.utils.ConfigUtils.mConfigCache;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.SwitchPreference;
//import androidx.preference.SwitchPreferenceCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.poorld.badget.R;
import com.poorld.badget.entity.ConfigEntity;
import com.poorld.badget.entity.InteractionType;
import com.poorld.badget.entity.ItemAppEntity;
import com.poorld.badget.utils.CommonUtils;
import com.poorld.badget.utils.ConfigUtils;
import com.poorld.badget.utils.PkgManager;
import com.topjohnwu.superuser.ShellUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

public class AppSettingsAct extends AppCompatActivity {

    public static final String TAG = "AppSettingsActAppSettingsAct";
    public static final String cmdSoTemplate = "rm -rf /data/user/0/%s/app_libs/%s";

    private static ItemAppEntity mApp;

    private MaterialToolbar toolbar;

    public static final String EXTRA_PKG = "package";

    private static final int REQUEST_CODE_TYPE_SCRIPT = 9527;
    private static final int REQUEST_CODE_TYPE_SCRIPT_DIR = 9528;

    private static String mPackageName;

    public static void startAct(Context context, String pkg) {
        mPackageName = pkg;
        Intent intent = new Intent(context, AppSettingsAct.class);
        intent.putExtra(EXTRA_PKG, pkg);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("badget");

        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(getDrawable(R.drawable.baseline_arrow_back_24));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

    }

    private void copyJs(Intent data) {
        File file = CommonUtils.saveFileFromUri(data.getData(), this, ConfigUtils.getBadgetPackagePath(mPackageName).getPath());
        if (file != null && file.exists()) {
            Log.d(TAG, "saveFileFromUri success: " + file.getPath());
            ConfigEntity.PkgConfig pkgConfig = ConfigUtils.getPkgConfig(mPackageName);
            if (pkgConfig == null) {
                return;
            }
            pkgConfig.setJsPath(file.getPath());

            ConfigUtils.updatePkgConfig();



            runOnUiThread(() -> {
                Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
                AppSettingsFragment fragment = (AppSettingsFragment) getSupportFragmentManager().findFragmentById(R.id.settings_container);
                if (fragment != null) {
                    fragment.refreshPreferences();
                }
            });
        } else {
            runOnUiThread(() -> Toast.makeText(this, "添加失败", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: ");
        if (data == null) {
            return;
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_TYPE_SCRIPT
                || requestCode == REQUEST_CODE_TYPE_SCRIPT_DIR) {
                new Thread(() -> copyJs(data)).start();
            }
        }
    }

    public static class AppSettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

        private static final String KEY_PREF_APP = "pref_app";
        private static final String KEY_PREF_SWITCH_ENABLE = "pref_switch_enable";
        private static final String KEY_MANAGER_INTERACTION_TYPE = "manager_interaction_type";
        private static final String KEY_PREF_JS_PATH = "pref_js_path";
        private static final String KEY_PREF_SCRIPT_DIRECTORY = "pref_script_directory";

        private static final String KEY_INTERACTION_TYPES = "interaction_types";
        private static final String KEY_GADGET_VERSIONS = "gadget_list";

        Preference prefApp;
        Preference prefJs;
        Preference prefScriptDirectory;

        ListPreference prefType;
        SwitchPreference prefEnable;
        ListPreference prefVersion;

        PreferenceGroup managerGroup;
        private ConfigEntity.PkgConfig pkgConfig;

        private Map<String, Preference> mDirScriptPreferences = new ArrayMap<>();


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "AppSettingsFragment#onCreate: ");
            addPreferencesFromResource(R.xml.app_settings_preferences);

            prefApp = findPreference(KEY_PREF_APP);
            prefEnable = findPreference(KEY_PREF_SWITCH_ENABLE);

            prefVersion = findPreference(KEY_GADGET_VERSIONS);
            List<String> gadgetFiles = ConfigUtils.getGadgetLibNames();
            CharSequence mentries[] = new String[gadgetFiles.size()];
            CharSequence mentryValues[] = new String[gadgetFiles.size()];
            int i = 0;
            for (String mdata : gadgetFiles) {
                mentries[i] = mdata;
                mentryValues[i] = Integer.toString(i);
                i++;
            }
            prefVersion.setEntries(mentries);
            prefVersion.setEntryValues(mentryValues);

            prefJs = findPreference(KEY_PREF_JS_PATH);
            prefScriptDirectory = findPreference(KEY_PREF_SCRIPT_DIRECTORY);
            prefType = findPreference(KEY_INTERACTION_TYPES);
            managerGroup = findPreference(KEY_MANAGER_INTERACTION_TYPE);

            Intent intent = getActivity().getIntent();
            if (intent != null) {
                String pkg = intent.getStringExtra(EXTRA_PKG);
                mApp = PkgManager.getItemAppEntity(getActivity(), pkg);
                if (mApp != null) {
                    prefEnable.setOnPreferenceChangeListener(this);
                    prefVersion.setOnPreferenceChangeListener(this);
                    prefJs.setOnPreferenceClickListener(this);
                    prefScriptDirectory.setOnPreferenceClickListener(this);
                    prefType.setOnPreferenceChangeListener(this);

                    pkgConfig = ConfigUtils.getPkgConfig(mApp.getPackageName());
                    if (pkgConfig != null) {
                        prefEnable.setChecked(pkgConfig.isEnabled());
                    }
                    prefApp.setTitle(mApp.getAppName());
                    prefApp.setSummary(mApp.getPackageName());
                    Drawable drawable = CommonUtils.resizeDrawable(getContext(), mApp.getDrawable(), 30, 30);
                    prefApp.setIcon(drawable);

                    refreshPreferences();
                }
            }
        }

        @Override
        public void onResume() {
            super.onResume();

        }

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {

        }

        public void refreshPreferences() {
            if (pkgConfig != null) {
                InteractionType type = pkgConfig.getType();
                prefVersion.setSummary(pkgConfig.getGadgetVersion());
                //InteractionType type = pkgConfig.getType();
                if (type != null) {
                    updateInteractionTypePreferences(type);
                }

            }

        }

        private void updateInteractionTypePreferences(InteractionType interactionType) {
            clearDirScripts();

            int index = interactionType.getInteractionType();
            prefType.setValueIndex(index);
            String[] values = getResources().getStringArray(R.array.types_entries);
            prefType.setSummary(values[index]);

            if (interactionType == InteractionType.Listen) {
                prefType.setSummary(values[index] + " 127.0.0.1:27042");
                prefJs.setVisible(false);
                prefScriptDirectory.setVisible(false);
            } else if (interactionType == InteractionType.Connect) {
                prefType.setSummary(values[index] + " 127.0.0.1:27052");
                prefJs.setVisible(false);
                prefScriptDirectory.setVisible(false);
            } else if (interactionType == InteractionType.Script) {
                prefJs.setVisible(true);
                String jsPath = pkgConfig.getJsPath();
                if (!TextUtils.isEmpty(jsPath)) {
                    prefJs.setSummary(jsPath);
                } else {
                    prefJs.setSummary("无");
                }

                prefScriptDirectory.setVisible(false);
            } else if (interactionType == InteractionType.ScriptDirectory) {
                prefScriptDirectory.setVisible(true);
                prefJs.setVisible(false);
                refreshDirScripts();
            }
        }

        private void refreshDirScripts() {

            Log.d(TAG, "refreshDirScripts: ");
            if (mApp != null) {
                File[] Scripts = ConfigUtils.getDirScripts(mApp.getPackageName());
                if (Scripts == null) {
                    return;
                }

                for (File script : Scripts) {
                    String key = script.getName();

                    Preference item = new Preference(getContext());
                    item.setKey(key);
                    //item.setTitle(name);
                    item.setSummary(key);
                    item.setIcon(R.drawable.baseline_javascript_24);
                    item.setOnPreferenceClickListener(preference -> {
                        showDeleteDialog(key, script);
                        return true;
                    });
                    mDirScriptPreferences.put(key, item);
                    managerGroup.addPreference(item);

                }
            }
        }

        private void clearDirScripts() {
            if (mDirScriptPreferences.size() > 0) {
                for (Preference preference : mDirScriptPreferences.values()) {
                    managerGroup.removePreference(preference);
                }
                mDirScriptPreferences.clear();
            }
        }

        private void removeScriptByKey(String key) {
            Preference preference = mDirScriptPreferences.get(key);
            if (mDirScriptPreferences != null) {
                mDirScriptPreferences.remove(preference);
                managerGroup.removePreference(preference);
            }
        }


        private void showDeleteDialog(String key, File file) {
            new AlertDialog.Builder(getContext())
                    .setMessage(String.format("是否删除脚本 %s ?", file.getName()))
                    .setPositiveButton("确定", (dialogInterface, i) -> {
                        //file.delete();
                        boolean result = ShellUtils.fastCmdResult(("rm " + file.getPath()));
                        Log.d(TAG, "result.result: " + result);
                        if (result) {
                            removeScriptByKey(key);
                        }

                    })
                    .setNegativeButton("取消", (dialogInterface, i) -> {
                        dialogInterface.cancel();
                    }).show();
        }



        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
            Log.d(TAG, "onPreferenceChange: ");
            Log.d(TAG, pkgConfig.getPkgName());

            if (pkgConfig == null) {
                return false;
            }

            if (KEY_PREF_SWITCH_ENABLE.equals(preference.getKey())) {
                boolean flag =  (Boolean) newValue;
                pkgConfig.setEnabled(flag);
                pkgConfig.setAppName(mApp.getAppName());

                if(!flag){
                    boolean delete1 = com.topjohnwu.superuser.ShellUtils.fastCmdResult(String.format(cmdSoTemplate, pkgConfig.getPkgName(), ConfigUtils.getGadgetLibName(pkgConfig.getSoName())));
                    boolean delete2 = com.topjohnwu.superuser.ShellUtils.fastCmdResult(String.format(cmdSoTemplate, pkgConfig.getPkgName(), ConfigUtils.getGadgetConfigLibName(pkgConfig.getSoName())));

                    Log.d(TAG, "lib so removed！" + String.format(cmdSoTemplate, pkgConfig.getPkgName(), ConfigUtils.getGadgetLibName(pkgConfig.getSoName())) + "->" + delete1);
                    Log.d(TAG, "lib config so removed！" + String.format(cmdSoTemplate, pkgConfig.getPkgName(), ConfigUtils.getGadgetConfigLibName(pkgConfig.getSoName()))+ "->" + delete2);

                    // revert default
                    prefVersion.setSummary("");
                    prefType.setSummary("Script");
                    prefJs.setSummary("无");

                    mConfigCache.addPkgConfigs(pkgConfig.getPkgName(), null);

                }
                ConfigUtils.updatePkgConfig();

            } else if (KEY_INTERACTION_TYPES.equals(preference.getKey())) {
                int index = Integer.parseInt((String) newValue);
                Log.d(TAG, "index:" + index);

                InteractionType interactionType = InteractionType.fromAttr(index);
                Log.d(TAG, "type:" + interactionType);

                updateInteractionTypePreferences(interactionType);
                pkgConfig.setType(interactionType);
                ConfigUtils.updatePkgConfig();

            } else if (KEY_GADGET_VERSIONS.equals(preference.getKey())) {
                int index = Integer.parseInt((String) newValue);
                Log.d(TAG, "index:" + index);

                pkgConfig.setGadgetVersion((String) prefVersion.getEntries()[index]);
                prefVersion.setSummary((String) prefVersion.getEntries()[index]);
                ConfigUtils.updatePkgConfig();
            }
            return true;
        }

        @Override
        public boolean onPreferenceClick(@NonNull Preference preference) {
            if (KEY_PREF_JS_PATH.equals(preference.getKey())) {
                if (pkgConfig != null) {
                    CommonUtils.openAssignFolder(getActivity(), REQUEST_CODE_TYPE_SCRIPT);
                }
            } else if (KEY_PREF_SCRIPT_DIRECTORY.equals(preference.getKey())) {
                CommonUtils.openAssignFolder(getActivity(), REQUEST_CODE_TYPE_SCRIPT_DIR);
            }
            return false;
        }
    }
}