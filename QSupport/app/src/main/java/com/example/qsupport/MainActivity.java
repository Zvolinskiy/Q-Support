package com.example.qsupport;

import android.animation.LayoutTransition;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.example.qsupport.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;
import android.view.Menu;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
	
	private android.speech.tts.TextToSpeech tts;
	private View lastOverlayView = null;
	private android.graphics.drawable.GradientDrawable focusFrame;
	
	private JSONObject currentJson;
	
	static {
		System.loadLibrary("q-support");
	}
	
	public native byte[] encryptHybrid(byte[] data);
	public native byte[] decryptHybrid(byte[] data);
	public native String NENative();
	public native String ENNative();
	public native String HTNative();
	public native String HTINative();
	
	public native void initFilters(AssetManager assetManager);
	public native String analyzeMessage(String message, String lang);
	
    private String L = "ru"; 
    private float textSize;
    private boolean isStealth = false;
    private SharedPreferences p;
    private LinearLayout container;
    private EditText search;
    private TextView title;
    private int currentTab = R.id.nav_guide;
	
	private LinearLayout dynamicContainerVolunteer;
	private LinearLayout volunteersLayout;
	private LinearLayout mainContentLayout;
	
	private static final String PREFS_NAME = "ChatPrefs";
	private static final String KEY_CHAT_HISTORY = "history";
	private static final String KEY_IS_SEARCHING = "isSearching";
	
	BottomNavigationView nav;
	
	private String[] girlNames;
	
	JSONObject allow_object;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		
		initFilters(getAssets());
		
		focusFrame = new android.graphics.drawable.GradientDrawable();
		focusFrame.setColor(android.graphics.Color.TRANSPARENT);
		focusFrame.setStroke(dpToPx(4), Color.parseColor("#4CAF50"));
		focusFrame.setCornerRadius(dpToPx(8));
		
	    tts = new android.speech.tts.TextToSpeech(this, new android.speech.tts.TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				if (status != android.speech.tts.TextToSpeech.ERROR) {
					tts.setLanguage(new java.util.Locale("ru"));
				}
			}
		});

        p = getSharedPreferences("Q_DATA", MODE_PRIVATE);
        L = p.getString("L", "ru");
        textSize = p.getFloat("TS", 15f);
        isStealth = p.getBoolean("S", false);
		
		boolean isFirstRun = p.getBoolean("is_first_run", true);

		if (isFirstRun) {
			android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
			LinearLayout dialogLayout = new LinearLayout(this);
			dialogLayout.setOrientation(LinearLayout.VERTICAL);
			dialogLayout.setPadding(dpToPx(25), dpToPx(25), dpToPx(25), dpToPx(20));
			dialogLayout.setBackgroundResource(R.drawable.edit_text_bg);

			TextView dTitle = new TextView(this);
			dTitle.setText("Добро пожаловать в Q-Support");
			dTitle.setTextSize(20);
			dTitle.setTypeface(null, Typeface.BOLD);
			dTitle.setTextColor(Color.parseColor("#1976D2"));
			dTitle.setPadding(0, 0, 0, dpToPx(12));

			TextView dMsg = new TextView(this);
			dMsg.setText("Q-Support - это социальное приложение, созданное для оказания оперативной помощи в экстренных ситуациях. Наша цель — спасать жизни через доступность информации.\n\n• Инструкции: Пошаговые алгоритмы первой помощи, когда каждая секунда на счету.\n• Поиск служб: Мгновенный доступ к номерам экстренных ведомств вашего региона.\n• Волонтеры: Единое сообщество людей, готовых прийти на помощь и поддержать в трудную минуту.\n• Настройки: Изменение размера шрифта, специальные возможности для людей с ограниченными способностями, встроенный TalkBack (озвучка интерфейса) и другие инструменты для комфортного использования.\n\nИзучите разделы заранее, чтобы быть готовым к любой ситуации.");
			dMsg.setTextSize(15);
			dMsg.setTextColor(Color.GRAY);
			dMsg.setLineSpacing(1.2f, 1.2f);

			TextView closeBtn = new TextView(this);
			closeBtn.setText("Ясно");
			closeBtn.setTextSize(14);
			closeBtn.setTypeface(null, Typeface.BOLD);
			closeBtn.setTextColor(Color.parseColor("#1976D2"));
			closeBtn.setGravity(Gravity.RIGHT);
			closeBtn.setPadding(dpToPx(10), dpToPx(20), dpToPx(10), 0);

			dialogLayout.addView(dTitle);
			dialogLayout.addView(dMsg);
			dialogLayout.addView(closeBtn);
			builder.setView(dialogLayout);

			final android.app.AlertDialog dialog = builder.create();

			closeBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						p.edit().putBoolean("is_first_run", false).apply();
						dialog.dismiss();
					}
				});

			if (dialog.getWindow() != null) {
				dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
			}
			dialog.show();
		}
		
		final ScrollView scrollD = findViewById(R.id.dynamicScollView);
		
        container = findViewById(R.id.dynamicContainer);
        search = findViewById(R.id.searchView);
        title = findViewById(R.id.headerTitle);
        nav = findViewById(R.id.bottomNav);
        Button bL = findViewById(R.id.langSwitch);
		
		volunteersLayout = findViewById(R.id.volunteersLayout);
		dynamicContainerVolunteer = findViewById(R.id.dynamicContainerVolunteer);
		mainContentLayout = (LinearLayout) findViewById(R.id.searchView).getParent(); 
		
        bL.setText(L.toUpperCase());
		
		if (p.getBoolean("VOICE_ENABLED", false)) {
			setTtsLanguage(L);
		}
		
        bL.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					L = L.equals("ru") ? "en" : (L.equals("en") ? "kz" : "ru");
					p.edit().putString("L", L).apply();
					((Button)v).setText(L.toUpperCase());
					
					if (p.getBoolean("VOICE_ENABLED", false)) {
						setTtsLanguage(L);
					}
					
					render();
				}
			});

        nav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
				@Override
				public boolean onNavigationItemSelected(@NonNull MenuItem item) {
					currentTab = item.getItemId();

					if (currentTab == R.id.nav_volunteers) {
						
						mainContentLayout.setVisibility(View.GONE);
						volunteersLayout.setVisibility(View.VISIBLE);

						renderVolunteers(); 
						speak(allow_object.optString("nav_1", "Раздел волонтёры. Нажмите найти волонтёра для связи."));
					} else {
						volunteersLayout.setVisibility(View.GONE);
						mainContentLayout.setVisibility(View.VISIBLE);

						render(); 

						if (currentTab == R.id.nav_guide) {
							
							scrollD.clearFocus();
							scrollD.post(new Runnable() {
									@Override
									public void run() {
										scrollD.fullScroll(ScrollView.FOCUS_UP);
									}
								});
								
							speak(allow_object.optString("nav_2", "База знаний"));
						}
						if (currentTab == R.id.nav_search) {
							
							scrollD.clearFocus();
							scrollD.post(new Runnable() {
									@Override
									public void run() {
										scrollD.fullScroll(ScrollView.FOCUS_UP);
									}
								});
							
							speak(allow_object.optString("nav_3", "Поиск"));
						}
						if (currentTab == R.id.nav_settings) speak(allow_object.optString("nav_4", "Настройки"));
					}
					return true;
				}
			});

        search.addTextChangedListener(new TextWatcher() {
				@Override public void afterTextChanged(Editable s) { render(); }
				@Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
				@Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {}
			});

		if (isStealth) {
			renderNotes();
		} else {
			render();
		}
    }

    private void render() {
        container.removeAllViews();
        String query = search.getText().toString().toLowerCase().trim();

        try {
            JSONObject json = new JSONObject(loadJSON());
            JSONObject str = json.getJSONObject("app_strings").getJSONObject(L);
			allow_object = str;
			
			if (allow_object != null) {
				girlNames = new String[] {
				
					allow_object.optString("alina", "Алина"),
					allow_object.optString("maria", "Мария"),
					allow_object.optString("darya", "Дарья"),
					allow_object.optString("aigerim", "Айгерим"),
					allow_object.optString("elena", "Елена"),
					allow_object.optString("kristina", "Кристина"),
					allow_object.optString("saule", "Сауле"),
					allow_object.optString("anna", "Анна"),
					allow_object.optString("viktoria", "Виктория"),
					allow_object.optString("diana", "Диана")
				};
			} else {
				girlNames = new String[] {
					
					"Алина", "Мария", "Дарья", "Айгерим", "Елена", 
					"Кристина", "Сауле", "Анна", "Виктория", "Диана"
				};
			}
			
			search.setHint(str.getString("search_title"));
			
			Menu menu = nav.getMenu();
			menu.findItem(R.id.nav_guide).setTitle(str.getString("nav_guide"));
			menu.findItem(R.id.nav_search).setTitle(str.getString("nav_search"));
			menu.findItem(R.id.nav_volunteers).setTitle(str.getString("nav_volunteers"));
			menu.findItem(R.id.nav_settings).setTitle(str.getString("nav_settings"));
			
            if (currentTab == R.id.nav_settings) {
                renderSettings(str);
            } else if (isStealth && currentTab == R.id.nav_guide) {
                title.setText(str.getString("stealth_title"));
                renderNotes();
            } else if (currentTab == R.id.nav_search) {
                title.setText(str.getString("title"));
                renderSearchAndContacts(json, query);
            } else if (currentTab == R.id.nav_volunteers) {
				allow_object = str;
                title.setText(str.getString("title"));
				renderVolunteers();
                
            } else {
                title.setText(str.getString("title"));
                renderGuides(json, query);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void renderGuides(JSONObject json, String query) throws Exception {
        search.setVisibility(View.GONE);
		final JSONObject str = json.getJSONObject("app_strings").getJSONObject(L);
		
		LinearLayout IHeader = new LinearLayout(this);
		IHeader.setOrientation(LinearLayout.HORIZONTAL);
		IHeader.setGravity(Gravity.CENTER);
		IHeader.setPadding(0, dpToPx(3), 0, dpToPx(3));

		TextView titleView = new TextView(this);
		titleView.setText(str.optString("base_header", "БАЗА ЗНАНИЙ").toUpperCase());
		titleView.setTextSize(15);
		titleView.setTypeface(null, Typeface.BOLD);
		titleView.setTextColor(Color.parseColor("#757575"));
		titleView.setLetterSpacing(0.05f);

		LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(-2, -2);
		titleView.setLayoutParams(titleParams);

		TextView helpBtn = new TextView(this);
		helpBtn.setText("?");
		helpBtn.setTextColor(Color.WHITE);
		helpBtn.setTextSize(12);
		helpBtn.setTypeface(null, Typeface.BOLD);
		helpBtn.setGravity(Gravity.CENTER);

		android.graphics.drawable.GradientDrawable circle = new android.graphics.drawable.GradientDrawable();
		circle.setShape(android.graphics.drawable.GradientDrawable.OVAL);
		circle.setColor(Color.parseColor("#1976D2"));
		helpBtn.setBackground(circle);

		int circleSize = dpToPx(20);
		LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(circleSize, circleSize);
		iconLp.setMargins(dpToPx(8), 0, 0, 0);
		helpBtn.setLayoutParams(iconLp);

		helpBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(v.getContext());
					LinearLayout dialogLayout = new LinearLayout(v.getContext());
					dialogLayout.setOrientation(LinearLayout.VERTICAL);
					dialogLayout.setPadding(dpToPx(25), dpToPx(25), dpToPx(25), dpToPx(20));
					dialogLayout.setBackgroundResource(R.drawable.edit_text_bg);

					TextView dTitle = new TextView(v.getContext());
					dTitle.setText(str.optString("help_title", "Инструкция"));
					dTitle.setTextSize(20);
					dTitle.setTypeface(null, Typeface.BOLD);
					dTitle.setTextColor(Color.parseColor("#1976D2"));
					dTitle.setPadding(0, 0, 0, dpToPx(12));

					TextView dMsg = new TextView(v.getContext());
					dMsg.setText(str.optString("help_message", "Данный раздел содержит официальные протоколы оказания первой помощи. \n\n1. Выберите нужную категорию.\n2. Нажмите на карточку для раскрытия алгоритма.\n3. Внимательно следуйте указаниям на экране до прибытия помощи."));
					dMsg.setTextSize(15);
					dMsg.setTextColor(Color.GRAY);
					dMsg.setLineSpacing(1.2f, 1.2f);

					TextView closeBtn = new TextView(v.getContext());
					closeBtn.setText(str.optString("btn_ok", "ПОНЯЛ"));
					closeBtn.setTextSize(14);
					closeBtn.setTypeface(null, Typeface.BOLD);
					closeBtn.setTextColor(Color.parseColor("#1976D2"));
					closeBtn.setGravity(Gravity.RIGHT);
					closeBtn.setPadding(dpToPx(10), dpToPx(20), dpToPx(10), 0);

					dialogLayout.addView(dTitle);
					dialogLayout.addView(dMsg);
					dialogLayout.addView(closeBtn);
					builder.setView(dialogLayout);

					final android.app.AlertDialog dialog = builder.create();

					closeBtn.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								dialog.dismiss();
							}
						});

					if (dialog.getWindow() != null) {
						dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
					}
					dialog.show();
				}
			});

		IHeader.addView(titleView);
		IHeader.addView(helpBtn);
		container.addView(IHeader);
		
        JSONArray guides = json.getJSONArray("guides");
        for (int i = 0; i < guides.length(); i++) {
            JSONObject g = guides.getJSONObject(i);
            addGuideCard(g.getString("t_" + L), g.getString("d_" + L), g.getString("secondTitle_" + L));
        }
    }

    private void renderSearchAndContacts(JSONObject json, String query) throws Exception {
		container.removeAllViews();
		search.setVisibility(View.VISIBLE);
		
		final JSONObject str = json.getJSONObject("app_strings").getJSONObject(L);
		
		int itemsCount = 0;
		String queryLower = query.toLowerCase().trim();

		if (!queryLower.isEmpty()) {
			JSONArray guides = json.getJSONArray("guides");
			for (int i = 0; i < guides.length(); i++) {
				JSONObject g = guides.getJSONObject(i);
				String t = g.getString("t_" + L);
				if (t.toLowerCase().contains(queryLower)) {
					addGuideCard(t, g.getString("d_" + L), g.getString("secondTitle_" + L));
					itemsCount++;
				}
			}
		}

		JSONArray content = json.getJSONArray("content");
		boolean hasContacts = false;

		for (int i = 0; i < content.length(); i++) {
			JSONArray items = content.getJSONObject(i).getJSONArray("items");
			for (int j = 0; j < items.length(); j++) {
				String name = items.getJSONObject(j).getString("name_" + L);
				if (queryLower.isEmpty() || name.toLowerCase().contains(queryLower)) {
					hasContacts = true;
					break;
				}
			}
			if (hasContacts) break;
		}

		if (hasContacts) {
			addHeader(str.optString("service_help", "Службы поддержки"));
		}

		for (int i = 0; i < content.length(); i++) {
			JSONArray items = content.getJSONObject(i).getJSONArray("items");
			for (int j = 0; j < items.length(); j++) {
				JSONObject it = items.getJSONObject(j);
				String name = it.getString("name_" + L);
				if (queryLower.isEmpty() || name.toLowerCase().contains(queryLower)) {
					addContactCard(name, it.getString("val"));
					itemsCount++;
				}
			}
		}

		if (itemsCount == 0 && !queryLower.isEmpty()) {
			TextView emptyText = new TextView(this);
			emptyText.setText(str.optString("search_404", "К сожалению, по вашему запросу\nничего не найдено"));
			emptyText.setGravity(Gravity.CENTER);
			emptyText.setTextColor(Color.parseColor("#757575"));
			emptyText.setTextSize(16f);

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
			);
			params.setMargins(0, dpToPx(50), 0, 0);
			emptyText.setLayoutParams(params);

			container.addView(emptyText);
		}
	}
	
	private void addHeader(String title) {
		TextView head = new TextView(this);
		head.setText(title);
		head.setTextSize(14f);
		head.setTextColor(Color.parseColor("#757575"));
		head.setTypeface(null, Typeface.BOLD);
		head.setAllCaps(true);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT, 
			LinearLayout.LayoutParams.WRAP_CONTENT
		);

		params.setMargins(dpToPx(4), dpToPx(20), 0, dpToPx(8)); 
		head.setLayoutParams(params);

		container.addView(head);
	}
	
    private void addGuideCard(final String t, final String d, final String title) {
		final LinearLayout card = new LinearLayout(this);
		card.setOrientation(LinearLayout.VERTICAL);
		card.setBackgroundResource(R.drawable.edit_text_bg);
		card.setPadding(40, 40, 40, 40);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
		lp.setMargins(0, 15, 0, 15);
		card.setLayoutParams(lp);
		
		TextView aboveCardTitle = new TextView(this);
		aboveCardTitle.setText(title.toUpperCase());
		aboveCardTitle.setTextSize(textSize - 4);
		aboveCardTitle.setTypeface(null, Typeface.BOLD);
		aboveCardTitle.setTextColor(Color.GRAY);

		LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-1, -2);
		titleLp.setMargins(10, 20, 0, 5);
		aboveCardTitle.setLayoutParams(titleLp);

		card.setContentDescription(allow_object.optString("show", "Статья: ") + t + allow_object.optString("click", ". Нажмите, чтобы развернуть"));

		RelativeLayout header = new RelativeLayout(this);
		header.setClickable(false);
		header.setFocusable(false);

		TextView titleTxt = new TextView(this);
		titleTxt.setText(t);
		titleTxt.setTextSize(textSize);
		titleTxt.setPadding(0, 0, dpToPx(40), 0);
		titleTxt.setEllipsize(TextUtils.TruncateAt.END);
		titleTxt.setTypeface(null, Typeface.BOLD);
		titleTxt.setTextColor(Color.parseColor("#1976D2"));
		titleTxt.setClickable(false);

		final ImageView arrow = new ImageView(this);
		arrow.setRotation(90f);
		arrow.setClickable(false);

		try {
			String fileName = "icons/q_arrow.png"; 
			InputStream ims = getAssets().open(fileName);
			Drawable di = Drawable.createFromStream(ims, null);
			arrow.setImageDrawable(di);
			arrow.setColorFilter(Color.parseColor("#9E9E9E"), PorterDuff.Mode.SRC_IN);
			ims.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		RelativeLayout.LayoutParams alp = new RelativeLayout.LayoutParams(dpToPx(15), dpToPx(15));
		alp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		alp.addRule(RelativeLayout.CENTER_VERTICAL);
		arrow.setLayoutParams(alp);

		header.addView(titleTxt);
		header.addView(arrow);

		final TextView body = new TextView(this);
		body.setText("\n" + d);
		body.setTextSize(textSize - 2);
		body.setVisibility(View.GONE);
		body.setClickable(false);

		card.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					boolean isVisible = (body.getVisibility() == View.VISIBLE);
					if (isVisible) {
						body.setVisibility(View.GONE);
						arrow.animate().rotation(90f).setDuration(300).start();
						card.setContentDescription(allow_object.optString("show", "Статья: ") + t + allow_object.optString("hidden", ". Свёрнуто"));
						speak(allow_object.optString("hidden", ". Свёрнуто"));
					} else {
						body.setVisibility(View.VISIBLE);
						arrow.animate().rotation(270f).setDuration(300).start();
						card.setContentDescription(allow_object.optString("isshow", "Развёрнуто. ") + t + ". " + d);
						speak(allow_object.optString("isshow", "Развёрнуто. ") + t + ". " + d);
					}
				}
			});

		card.addView(header);
		card.addView(body);
		
		container.addView(aboveCardTitle);
		container.addView(card);
	}
	
	private LinearLayout row;

	private void addContactCard(String name, final String val) {
		if (row == null || row.getChildCount() == 2) {
			row = new LinearLayout(this);
			row.setOrientation(LinearLayout.HORIZONTAL);
			LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, -2);
			rowParams.setMargins(0, 0, 0, dpToPx(12));
			row.setLayoutParams(rowParams);
			container.addView(row);
		}

		androidx.appcompat.widget.AppCompatButton b = new androidx.appcompat.widget.AppCompatButton(this);
		b.setText(name + "\n(" + val + ")");
		b.setBackgroundResource(R.drawable.btn_main_bg);
		b.setTextColor(Color.WHITE);
		b.setAllCaps(false);
		b.setGravity(Gravity.CENTER);
		b.setLineSpacing(0, 1.2f);

		int pSides = dpToPx(8);
		int pTopBottom = dpToPx(20);
		b.setPadding(pSides, pTopBottom, pSides, pTopBottom);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -1, 1f);

		if (row.getChildCount() == 0) {
			params.setMargins(0, 0, dpToPx(12), 0);
		} else {
			params.setMargins(0, 0, 0, 0);
		}

		b.setLayoutParams(params);

		b.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_DIAL);
					intent.setData(Uri.parse("tel:" + val));
					startActivity(intent);
				}
			});

		row.addView(b);
	}
	
	private void renderVolunteers() {
		dynamicContainerVolunteer.removeAllViews();

		String history = p.getString("chat_history", "");
		if (!history.isEmpty()) {
			startMiniChat(true);
			return;
		}

		dynamicContainerVolunteer.setGravity(Gravity.CENTER);

		AppCompatButton btnFind = new AppCompatButton(this);
		btnFind.setText(allow_object.optString("find_volont", "Найти волонтера"));
		btnFind.setTextColor(Color.parseColor("#1976D2"));
		btnFind.setAllCaps(false);
		btnFind.setTextSize(17);
		btnFind.setElevation(10);
		btnFind.setBackgroundResource(R.drawable.btn_lang_bg);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(240), dpToPx(60));
		btnFind.setLayoutParams(params);

		btnFind.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View v) {
					v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction(new Runnable() {
							@Override
							public void run() {
								if (findViewById(R.id.bottomNav) != null) {
									findViewById(R.id.bottomNav).setVisibility(View.GONE);
								}
								v.animate().scaleX(1f).scaleY(1f).setDuration(100);
								showLoadingState();
							}
						});
				}
			});

		dynamicContainerVolunteer.addView(btnFind);
	}

	private void showLoadingState() {
		dynamicContainerVolunteer.removeAllViews();
		dynamicContainerVolunteer.setGravity(Gravity.CENTER);

		if (dynamicContainerVolunteer instanceof LinearLayout) {
			((LinearLayout) dynamicContainerVolunteer).setOrientation(LinearLayout.VERTICAL);
		}

		ProgressBar pb = new ProgressBar(this);
		LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
		);
		pbParams.gravity = Gravity.CENTER_HORIZONTAL;
		pb.setLayoutParams(pbParams);
		pb.getIndeterminateDrawable().setColorFilter(Color.parseColor("#2196F3"), android.graphics.PorterDuff.Mode.SRC_IN);

		TextView tvStatus = new TextView(this);
		tvStatus.setText(allow_object.optString("find_all", "Ищем волонтёра..."));
		tvStatus.setTextSize(16);
		tvStatus.setTextColor(Color.parseColor("#808080"));
		tvStatus.setPadding(0, dpToPx(20), 0, 0);
		tvStatus.setGravity(Gravity.CENTER);

		LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
		);
		tvParams.gravity = Gravity.CENTER_HORIZONTAL;
		tvStatus.setLayoutParams(tvParams);

		dynamicContainerVolunteer.addView(pb);
		dynamicContainerVolunteer.addView(tvStatus);

		new android.os.Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					startMiniChat(false);
				}
			}, 3500);
	}
	
	private String currentBotName;
	
	private View createMiniActionBar() {
		LinearLayout actionBar = new LinearLayout(this);
		actionBar.setOrientation(LinearLayout.HORIZONTAL);
		actionBar.setGravity(Gravity.CENTER_VERTICAL);
		int paddingH = dpToPx(16);
		int paddingV = dpToPx(8);
		actionBar.setPadding(paddingH, paddingV, paddingH, paddingV);
		actionBar.setBackgroundColor(Color.parseColor("#FFFFFF"));
		actionBar.setElevation(dpToPx(4));

		FrameLayout avatarContainer = new FrameLayout(this);

		androidx.cardview.widget.CardView avatarFrame = new androidx.cardview.widget.CardView(this);
		avatarFrame.setRadius(dpToPx(20));
		avatarFrame.setCardElevation(0);

		ImageView avatar = new ImageView(this);
		avatar.setImageResource(android.R.drawable.ic_menu_myplaces); 
		avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
		avatar.setColorFilter(Color.parseColor("#673AB7")); 
		avatarFrame.addView(avatar);

		final View statusDot = new View(this);
		android.graphics.drawable.GradientDrawable dotDrawable = new android.graphics.drawable.GradientDrawable();
		dotDrawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
		dotDrawable.setColor(Color.parseColor("#4CAF50"));
		dotDrawable.setStroke(dpToPx(2), Color.WHITE);
		statusDot.setBackground(dotDrawable);

		avatarContainer.addView(avatarFrame, dpToPx(40), dpToPx(40));

		FrameLayout.LayoutParams dotParams = new FrameLayout.LayoutParams(dpToPx(12), dpToPx(12));
		dotParams.gravity = Gravity.BOTTOM | Gravity.END;
		dotParams.setMargins(0, 0, dpToPx(2), dpToPx(2));
		avatarContainer.addView(statusDot, dotParams);

		android.view.animation.AlphaAnimation pulse = new android.view.animation.AlphaAnimation(0.4f, 1.0f);
		pulse.setDuration(1000);
		pulse.setRepeatMode(android.view.animation.Animation.REVERSE);
		pulse.setRepeatCount(android.view.animation.Animation.INFINITE);
		statusDot.startAnimation(pulse);

		LinearLayout textContainer = new LinearLayout(this);
		textContainer.setOrientation(LinearLayout.VERTICAL);
		textContainer.setPadding(dpToPx(12), 0, 0, 0);

		TextView nameText = new TextView(this);
		nameText.setText(currentBotName + " (Q-Support AI)");
		nameText.setTextColor(Color.BLACK);
		nameText.setTextSize(16);
		nameText.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));

		TextView statusText = new TextView(this);
		statusText.setText(allow_object.optString("q_online", "В сети"));
		statusText.setTextColor(Color.parseColor("#4CAF50"));
		statusText.setTextSize(12);

		textContainer.addView(nameText);
		textContainer.addView(statusText);

		actionBar.addView(avatarContainer);
		actionBar.addView(textContainer);

		return actionBar;
	}
	
	private void startMiniChat(boolean fromHistory) {
		
		if (findViewById(R.id.bottomNav) != null) {
			findViewById(R.id.bottomNav).setVisibility(View.VISIBLE);
		}
		
		dynamicContainerVolunteer.removeAllViews();
		dynamicContainerVolunteer.setGravity(Gravity.TOP);
		dynamicContainerVolunteer.setLayoutTransition(new LayoutTransition());

		RelativeLayout chatRoot = new RelativeLayout(this);
		chatRoot.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

		final LinearLayout chatContent = new LinearLayout(this);
		chatContent.setOrientation(LinearLayout.VERTICAL);
		chatContent.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));

		if (!fromHistory) {
			currentBotName = girlNames[new java.util.Random().nextInt(girlNames.length)];
		} else {
			String history = p.getString("chat_history", "");
			if (!history.isEmpty()) {
				try {
					String firstMsg = history.split("SPLIT_MSG")[0];
					if (firstMsg.startsWith("B:")) {
						String savedName = firstMsg.split(":")[1].trim();

						int index = 0;
						String[] ref = {
							"Алина", "Мария", "Дарья", "Айгерим", "Елена", "Кристина", "Сауле", "Анна", "Виктория", "Диана",
							"Әлина", "Мәрия", "Дария", "Әйгерім", "Елена", "Кристина", "Сәуле", "Анна", "Виктория", "Диана",
							"Alina", "Maria", "Darya", "Aigerim", "Yelena", "Kristina", "Saule", "Anna", "Victoria", "Diana"
						};

						for (int i = 0; i < ref.length; i++) {
							if (ref[i].equalsIgnoreCase(savedName)) {
								index = i % 10;
								break;
							}
						}

						if (index < girlNames.length) {
							currentBotName = girlNames[index];
						} else {
							currentBotName = girlNames[0];
						}
					} else {
						currentBotName = girlNames[0];
					}
				} catch (Exception e) {
					currentBotName = (girlNames != null && girlNames.length > 0) ? girlNames[0] : "AI";
				}
			} else {
				currentBotName = (girlNames != null && girlNames.length > 0) ? girlNames[0] : "AI";
			}
		}
		
		View actionBar = createMiniActionBar();
		actionBar.setId(View.generateViewId());
		RelativeLayout.LayoutParams actionParams = new RelativeLayout.LayoutParams(-1, -2);
		actionParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		actionBar.setLayoutParams(actionParams);

		final LinearLayout inputBar = new LinearLayout(this);
		inputBar.setId(View.generateViewId());
		inputBar.setOrientation(LinearLayout.HORIZONTAL);
		inputBar.setGravity(Gravity.CENTER_VERTICAL);
		inputBar.setPadding(dpToPx(8), 0, dpToPx(8), 0);
		GradientDrawable shape = new GradientDrawable();
		shape.setColor(Color.parseColor("#F8F9FA"));
		shape.setCornerRadius(dpToPx(25));
		shape.setStroke(dpToPx(1), Color.parseColor("#E0E0E0"));
		inputBar.setBackground(shape);

		RelativeLayout.LayoutParams barParams = new RelativeLayout.LayoutParams(-1, dpToPx(50));
		barParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		barParams.setMargins(dpToPx(10), dpToPx(5), dpToPx(10), dpToPx(10));
		inputBar.setLayoutParams(barParams);

		ImageView imgIcon = new ImageView(this);
		imgIcon.setImageResource(R.drawable.ic_attach);
		imgIcon.setColorFilter(Color.parseColor("#757575"));
		imgIcon.setPadding(dpToPx(8), 0, dpToPx(8), 0);

		final EditText input = new EditText(this);
		input.setHint(allow_object.optString("messange_label", "Сообщение"));
		input.setHintTextColor(Color.parseColor("#9E9E9E"));
		input.setBackground(null);
		input.setTextSize(15);
		input.setPadding(dpToPx(5), 0, dpToPx(5), 0);
		LinearLayout.LayoutParams iParams = new LinearLayout.LayoutParams(0, -1, 1f);
		input.setLayoutParams(iParams);

		final ImageView sendBtn = new ImageView(this);
		sendBtn.setImageResource(R.drawable.ic_send);
		sendBtn.setColorFilter(Color.parseColor("#2196F3"));
		sendBtn.setPadding(dpToPx(8), 0, dpToPx(8), 0);

		inputBar.addView(imgIcon, dpToPx(40), -1);
		inputBar.addView(input);
		inputBar.addView(sendBtn, dpToPx(40), -1);

		final HorizontalScrollView suggestScroll = new HorizontalScrollView(this);
		suggestScroll.setId(View.generateViewId());
		suggestScroll.setHorizontalScrollBarEnabled(false);
		RelativeLayout.LayoutParams scrollParams2 = new RelativeLayout.LayoutParams(-1, -2);
		scrollParams2.addRule(RelativeLayout.ABOVE, inputBar.getId());
		scrollParams2.setMargins(dpToPx(8), 0, dpToPx(8), dpToPx(4));
		suggestScroll.setLayoutParams(scrollParams2);

		final LinearLayout suggestionsLayout = new LinearLayout(this);
		suggestionsLayout.setOrientation(LinearLayout.HORIZONTAL);
		suggestionsLayout.setGravity(Gravity.CENTER);
		String[] suggestions = {allow_object.optString("section_1", "Как это работает?"), allow_object.optString("section_2", "Нужна помощь"), allow_object.optString("section_3", "О приложении")};

		for (int i = 0; i < suggestions.length; i++) {
			final String text = suggestions[i];
			TextView chip = new TextView(this);
			chip.setText(text);
			chip.setTextColor(Color.parseColor("#2196F3"));
			chip.setTextSize(13);
			chip.setPadding(dpToPx(14), dpToPx(7), dpToPx(14), dpToPx(7));
			GradientDrawable chipShape = new GradientDrawable();
			chipShape.setColor(Color.WHITE);
			chipShape.setCornerRadius(dpToPx(18));
			chipShape.setStroke(dpToPx(1), Color.parseColor("#2196F3"));
			chip.setBackground(chipShape);
			LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, -2);
			p.setMargins(dpToPx(4), 0, dpToPx(4), 0);
			chip.setLayoutParams(p);
			chip.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						input.setText(text);
						sendBtn.performClick();
						suggestScroll.setVisibility(View.GONE);
					}
				});
			suggestionsLayout.addView(chip);
		}
		suggestScroll.addView(suggestionsLayout);

		final ScrollView scrollView = new ScrollView(this);
		scrollView.setVerticalScrollBarEnabled(false);
		RelativeLayout.LayoutParams chatParams = new RelativeLayout.LayoutParams(-1, -1);
		chatParams.addRule(RelativeLayout.BELOW, actionBar.getId());
		chatParams.addRule(RelativeLayout.ABOVE, suggestScroll.getId());
		scrollView.setLayoutParams(chatParams);
		scrollView.addView(chatContent);
		
		if (!fromHistory) {
			String welcomePart = allow_object.optString("bot_start", "Здравствуйте! Я на связи. Чем я могу вам помочь?");

			if (welcomePart.startsWith(":")) {
				welcomePart = welcomePart.substring(1).trim();
			}

			String welcomeFull = currentBotName + ": " + welcomePart;
			addMessage(chatContent, welcomeFull, false);
			saveMessage("B:" + currentBotName + ":" + welcomePart);

		} else {
			String history = p.getString("chat_history", "");
			String[] msgs = history.split("SPLIT_MSG");

			for (String m : msgs) {
				if (m.isEmpty()) continue;

				if (m.startsWith("U:")) {
					String textOnly = m.substring(2);
					String prefixYou = allow_object.optString("msg_you", "Вы");

					if (prefixYou.endsWith(": ")) {
						addMessage(chatContent, prefixYou + textOnly, true);
					} else {
						addMessage(chatContent, prefixYou + ": " + textOnly, true);
					}

				} else if (m.startsWith("B:")) {
					String[] parts = m.split(":", 3);
					if (parts.length >= 3) {
						String botName = parts[1];
						String botText = parts[2].trim();

						if (botText.startsWith(":")) {
							botText = botText.substring(1).trim();
						}

						addMessage(chatContent, botName + ": " + botText, false);
					}
				}
			}
		}
		
		imgIcon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String message = allow_object.optString("dev_toast", "Данная функция в разработке");
					Toast.makeText(v.getContext(), message, Toast.LENGTH_SHORT).show();
				}
			});
			
		sendBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final String rawMsg = input.getText().toString().trim();
					final String msgForAnalysis = rawMsg.toLowerCase(java.util.Locale.ROOT);

					if (!rawMsg.isEmpty()) {
						suggestScroll.setVisibility(View.GONE);

						String prefixYou = allow_object.optString("msg_you", "Вы");
						if (prefixYou.contains(":")) {
							prefixYou = prefixYou.replace(":", "").trim();
						}

						addMessage(chatContent, prefixYou + ": " + rawMsg, true);
						saveMessage("U:" + rawMsg);
						input.setText("");

						final String aiResponse = analyzeMessage(msgForAnalysis, L);

						new android.os.Handler().postDelayed(new Runnable() {
								@Override
								public void run() {
									String cleanAiResponse = aiResponse.trim();
									if (cleanAiResponse.startsWith(":")) {
										cleanAiResponse = cleanAiResponse.substring(1).trim();
									}

									String fullAiMsg = currentBotName + ": " + cleanAiResponse;
									addMessage(chatContent, fullAiMsg, false);

									saveMessage("B:" + currentBotName + ":" + cleanAiResponse);

									scrollView.post(new Runnable() {
											@Override
											public void run() {
												scrollView.fullScroll(View.FOCUS_DOWN);
											}
										});
								}
							}, 600);
					}
				}
			});

		chatRoot.addView(actionBar);
		chatRoot.addView(scrollView);
		chatRoot.addView(suggestScroll);
		chatRoot.addView(inputBar);

		dynamicContainerVolunteer.addView(chatRoot);
	}

	private void addMessage(LinearLayout container, String text, boolean isUser) {
		TextView bubble = new TextView(this);
		bubble.setText(text);
		bubble.setTextSize(15);
		bubble.setPadding(dpToPx(16), dpToPx(10), dpToPx(16), dpToPx(10));
		bubble.setMaxWidth(dpToPx(260));

		GradientDrawable shape = new GradientDrawable();
		shape.setCornerRadius(dpToPx(20));

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
		lp.setMargins(0, dpToPx(4), 0, dpToPx(4));

		if (isUser) {
			shape.setColor(Color.parseColor("#2196F3"));
			bubble.setTextColor(Color.WHITE);
			lp.gravity = Gravity.END;
		} else {
			shape.setColor(Color.parseColor("#E9E9EB"));
			bubble.setTextColor(Color.BLACK);
			lp.gravity = Gravity.START;
		}

		bubble.setBackground(shape);
		bubble.setLayoutParams(lp);

		bubble.setAlpha(0f);
		bubble.setTranslationY(30f);
		container.addView(bubble);
		bubble.animate().alpha(1f).translationY(0f).setDuration(250).start();
	}

	private void saveMessage(String msg) {
		String history = p.getString("chat_history", "");
		p.edit().putString("chat_history", history + msg + "SPLIT_MSG").apply();
	}

	private void loadHistory(LinearLayout container) {
		String history = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(KEY_CHAT_HISTORY, "");
		String[] msgs = history.split("\\|\\|\\|");
		for (String m : msgs) {
			if (!m.isEmpty()) {
				addMessage(container, m, m.startsWith("Вы:"));
			}
		}
	}

	private void renderSettings(final JSONObject str) throws Exception {
        currentJson = str; 
        container.removeAllViews();
        search.setVisibility(View.GONE);
        title.setVisibility(View.VISIBLE);
        ((View)container.getParent()).setBackgroundColor(Color.WHITE);

        final float currentTS = p.getFloat("TS", 15f);
        textSize = currentTS;

        title.setText(str.getString("settings"));
        title.setTextSize(currentTS + 4);

        Button btnS = new Button(this);
        btnS.setText(str.optString("custom_ui", "Персонализация интерфейса"));
        btnS.setBackgroundResource(R.drawable.btn_main_bg);
        btnS.setTextColor(Color.WHITE);
        btnS.setTextSize(currentTS - 2);
        btnS.setAllCaps(false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dpToPx(10), 0, dpToPx(10));
        btnS.setLayoutParams(lp);
        btnS.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					isStealth = true;
					p.edit().putBoolean("S", true).apply();
					renderNotes();
				}
			});
        container.addView(btnS);

        TextView fLabel = new TextView(this);
        fLabel.setText("\n" + str.getString("font_size") + ": " + (int)currentTS);
        fLabel.setTextSize(currentTS);
        fLabel.setTypeface(null, Typeface.BOLD);
        container.addView(fLabel);

        SeekBar sb = new SeekBar(this);
        sb.setMax(14);
        sb.setProgress((int)currentTS - 14);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override public void onProgressChanged(SeekBar s, int pr, boolean f) {
					if (f) {
						float next = 14f + pr;
						p.edit().putFloat("TS", next).apply();
						try { renderSettings(currentJson); } catch (Exception e) {}
					}
				}
				@Override public void onStartTrackingTouch(SeekBar s) {}
				@Override public void onStopTrackingTouch(SeekBar s) {}
			});
        container.addView(sb);
		
		TextView accessibilityLabel = new TextView(this);
		accessibilityLabel.setText(str.optString("accessibility", "Специальные возможности"));
		accessibilityLabel.setTextSize(currentTS);
		accessibilityLabel.setTypeface(null, Typeface.BOLD);
		accessibilityLabel.setPadding(0, dpToPx(15), 0, dpToPx(5));
		container.addView(accessibilityLabel);

		CheckBox voiceCb = new CheckBox(this);
		voiceCb.setButtonDrawable(R.drawable.custom_checkbox_selector);
		voiceCb.setBackgroundColor(android.graphics.Color.TRANSPARENT);

		float density = getResources().getDisplayMetrics().density;
		int paddingLeft = (int) (12 * density);
		voiceCb.setPadding(paddingLeft, 0, 0, 0);

		voiceCb.setText(str.optString("voice_assistant", "Голосовой помощник"));
		voiceCb.setTextSize(currentTS - 2);
		voiceCb.setChecked(p.getBoolean("VOICE_ENABLED", false));

		voiceCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
					buttonView.animate()
						.scaleX(0.92f)
						.scaleY(0.92f)
						.setDuration(120)
						.setInterpolator(new android.view.animation.DecelerateInterpolator())
						.withEndAction(new Runnable() {
							@Override
							public void run() {
								buttonView.animate()
									.scaleX(1.0f)
									.scaleY(1.0f)
									.setDuration(200)
									.setInterpolator(new android.view.animation.OvershootInterpolator(1.5f))
									.start();
							}
						}).start();

					p.edit().putBoolean("VOICE_ENABLED", isChecked).apply();

					if (isChecked) {
						
						setTtsLanguage(L);
						
						focusFrame.setStroke(dpToPx(4), Color.parseColor("#4CAF50"));
						
						tts.speak(str.optString("voice_enb", "Голосовое сопровождение активировано. Я буду помогать вам в работе с приложением."), 
								  android.speech.tts.TextToSpeech.QUEUE_FLUSH, null);
					} else {
						focusFrame.setStroke(0, Color.TRANSPARENT);
						focusFrame.setColor(Color.TRANSPARENT);
						
						buttonView.setBackground(null);
						if (tts != null) {
							tts.stop();
						}
					}
				}
			});
			
		container.addView(voiceCb);

		Button btnDelete = new Button(this);
		btnDelete.setText(str.optString("delete_all", "Удалить все данные"));

		GradientDrawable deleteShape = new GradientDrawable();
		deleteShape.setCornerRadius(dpToPx(12));
		deleteShape.setColor(Color.parseColor("#C62828"));
		btnDelete.setBackground(deleteShape);

		btnDelete.setTextColor(Color.WHITE);
		btnDelete.setTextSize(currentTS - 4);
		btnDelete.setAllCaps(true);

		LinearLayout.LayoutParams deleteLp = new LinearLayout.LayoutParams(-1, -2);
		deleteLp.setMargins(0, dpToPx(20), 0, dpToPx(10));
		btnDelete.setLayoutParams(deleteLp);

		btnDelete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(v.getContext());
					
					LinearLayout dialogLayout = new LinearLayout(v.getContext());
					dialogLayout.setOrientation(LinearLayout.VERTICAL);
					dialogLayout.setPadding(dpToPx(25), dpToPx(25), dpToPx(25), dpToPx(10));

					TextView dTitle = new TextView(v.getContext());
					dTitle.setText(str.optString("help_title2", "Полный сброс"));
					dTitle.setTextSize(20);
					dTitle.setTypeface(null, Typeface.BOLD);
					dTitle.setTextColor(Color.parseColor("#1976D2"));
					dTitle.setPadding(0, 0, 0, dpToPx(12));

					TextView dMsg = new TextView(v.getContext());
					dMsg.setText(str.optString("help_message2", "Внимание! Удалится всё: настройки, кэш приложения и все зашифрованные тексты. Это действие необратимо."));
					dMsg.setTextSize(15);
					dMsg.setTextColor(Color.GRAY);
					dMsg.setLineSpacing(1.2f, 1.2f);

					dialogLayout.addView(dTitle);
					dialogLayout.addView(dMsg);

					builder.setView(dialogLayout);

					builder.setPositiveButton(str.optString("btn_ok2", "Да, стереть всё"), new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(android.content.DialogInterface dialog, int which) {
								if (tts != null) {
									tts.stop();
								}

								p.edit().clear().apply();

								p.edit()
									.putString("TEXT_SIZE", "15")
									.putBoolean("VOICE_ENABLED", false)
									.putBoolean("S", false)
									.apply();

								isStealth = false;
								textSize = 15f;
								currentJson = null;

								Toast.makeText(MainActivity.this, str.optString("success_delete", "Приложение полностью очищено"), Toast.LENGTH_LONG).show();

								recreate();
							}
						});

					builder.setNegativeButton(str.optString("btn_cancel", "Отмена"), new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(android.content.DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});

					final android.app.AlertDialog dialog = builder.create();

					if (dialog.getWindow() != null) {
						dialog.getWindow().setBackgroundDrawableResource(R.drawable.edit_text_bg);
					}

					dialog.show();

					dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.TRANSPARENT);
					dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);

					dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.TRANSPARENT);
					dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#1976D2"));
				}
			});
		container.addView(btnDelete);

        View spacer = new View(this);
        container.addView(spacer, new LinearLayout.LayoutParams(0, 0, 1.0f));

        addDetail(HTNative().toString(), NENative().toString(), currentTS);
        addDetail(HTINative().toString(), ENNative().toString(), currentTS);

        TextView bug = new TextView(this);
        bug.setText(str.optString("bug_report", "Нашли баг? Сообщите его нам"));
        bug.setTextColor(Color.parseColor("#1976D2"));
        bug.setTextSize(currentTS - 2);
        bug.setPadding(0, 5, 0, 30);
        bug.setGravity(Gravity.CENTER);
        bug.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:dev@qsupport.com")));
				}
			});
        container.addView(bug);
    }
	
	private void renderNotes() {
		container.removeAllViews();
		search.setVisibility(View.GONE);
		title.setVisibility(View.GONE);

		if (findViewById(R.id.bottomNav) != null) {
			findViewById(R.id.bottomNav).setVisibility(View.GONE);
		}
		
		if (findViewById(R.id.topBar) != null) {
			findViewById(R.id.topBar).setVisibility(View.GONE);
		}

		ScrollView scrollView = new ScrollView(this);
		scrollView.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1.0f));
		scrollView.setFillViewport(true);

		LinearLayout scrollContent = new LinearLayout(this);
		scrollContent.setOrientation(LinearLayout.VERTICAL);

		final EditText ed = new EditText(this);
		
		String savedBase64 = p.getString("CIPHER_DATA", "");
		if (savedBase64 != null && !savedBase64.isEmpty()) {
			try {
				byte[] decoded = android.util.Base64.decode(savedBase64, android.util.Base64.DEFAULT);
				byte[] decrypted = decryptHybrid(decoded);

				if (decrypted != null) {
					ed.setText(new String(decrypted, "UTF-8"));
				}
			} catch (Exception e) {
				ed.setText("");
			}
		}
		

		String hint = "Личные заметки...";
		if (allow_object != null) {
			hint = allow_object.optString("my_notes", hint);
		}
		ed.setHint(hint);

		ed.setTextSize(textSize);
		ed.setGravity(Gravity.TOP);
		ed.setBackground(null);
		ed.setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20));

		LinearLayout.LayoutParams edLp = new LinearLayout.LayoutParams(-1, -2);
		ed.setLayoutParams(edLp);

		scrollContent.addView(ed);
		scrollView.addView(scrollContent);

		Button exit = new Button(this);

		String btnText = "Завершить сессию";
		if (allow_object != null) {
			btnText = allow_object.optString("closed_session", btnText);
		}
		exit.setText(btnText);

		exit.setBackgroundResource(R.drawable.btn_main_bg);
		exit.setTextColor(Color.WHITE);
		exit.setAllCaps(false);

		LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(-1, -2);
		btnLp.setMargins(dpToPx(16), dpToPx(10), dpToPx(16), dpToPx(16));
		exit.setLayoutParams(btnLp);

		exit.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String txt = ed.getText().toString();
					try {
						if (txt != null && !txt.trim().isEmpty()) {
							byte[] raw = txt.getBytes("UTF-8");
							byte[] encrypted = encryptHybrid(raw);

							if (encrypted != null) {
								String base64 = android.util.Base64.encodeToString(encrypted, android.util.Base64.DEFAULT);
								p.edit().putString("CIPHER_DATA", base64).apply();
							}
						} else {
							p.edit().putString("CIPHER_DATA", "").apply();
						}
					} catch (Exception e) {
						android.util.Log.e("JNI_CRYPTO", "Encryption error", e);
					}

					isStealth = false;
					p.edit().putBoolean("S", false).apply();

					if (findViewById(R.id.bottomNav) != null) findViewById(R.id.bottomNav).setVisibility(View.VISIBLE);
					if (findViewById(R.id.topBar) != null) findViewById(R.id.topBar).setVisibility(View.VISIBLE);

					try {
						renderSettings(currentJson);
					} catch (Exception e) {
						render();
					}
				}
			});

		container.addView(scrollView);
		container.addView(exit);
	}
	
	private void addDetail(String key, String value, float ts) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dpToPx(5), 0, dpToPx(5));

        TextView k = new TextView(this);
        k.setText(key + ": ");
        k.setTextSize(ts - 6);
        k.setTextColor(Color.parseColor("#757575"));
        k.setTypeface(null, Typeface.BOLD);

        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(ts - 6);
        v.setTextColor(Color.parseColor("#1976D2"));

        row.addView(k);
        row.addView(v);

        container.addView(row);
    }
	
	@Override
	public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
		if (ev.getAction() == android.view.MotionEvent.ACTION_DOWN && p.getBoolean("VOICE_ENABLED", false)) {
			int x = (int) ev.getRawX();
			int y = (int) ev.getRawY();

			View bottomNav = findViewById(R.id.bottomNav);
			int[] navLoc = new int[2];
			bottomNav.getLocationOnScreen(navLoc);

			View targetView;
			if (y >= navLoc[1]) {
				targetView = findViewAt((ViewGroup) bottomNav, x, y);
			} else {
				targetView = findViewAt((ViewGroup) getWindow().getDecorView().getRootView(), x, y);
			}

			if (targetView != null) {
				String text = "";
				if (targetView.getContentDescription() != null) {
					text = targetView.getContentDescription().toString();
				} else if (targetView instanceof TextView) {
					text = ((TextView) targetView).getText().toString();
				} else if (targetView instanceof EditText) {
					text = "Поле ввода";
				}

				if (text.trim().isEmpty() || targetView instanceof ViewGroup && !(targetView instanceof android.widget.Checkable)) {
					if (lastOverlayView != null) {
						lastOverlayView.getOverlay().clear();
						lastOverlayView = null;
					}
					return super.dispatchTouchEvent(ev); 
				}

				if (lastOverlayView != null) {
					lastOverlayView.getOverlay().clear();
				}

				focusFrame.setBounds(0, 0, targetView.getWidth(), targetView.getHeight());
				targetView.getOverlay().add(focusFrame);
				lastOverlayView = targetView;

				speak(text);
			} else {
				if (lastOverlayView != null) {
					lastOverlayView.getOverlay().clear();
					lastOverlayView = null;
				}
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	private String findTextInContainer(ViewGroup group) {
		for (int i = 0; i < group.getChildCount(); i++) {
			View child = group.getChildAt(i);
			if (child instanceof TextView) return ((TextView) child).getText().toString();
			if (child instanceof ViewGroup) return findTextInContainer((ViewGroup) child);
		}
		return "";
	}

	private View findViewAt(ViewGroup container, int x, int y) {
		for (int i = 0; i < container.getChildCount(); i++) {
			View child = container.getChildAt(i);
			if (!child.isShown()) continue;

			int[] location = new int[2];
			child.getLocationOnScreen(location);

			if (x >= location[0] && x <= location[0] + child.getWidth() &&
				y >= location[1] && y <= location[1] + child.getHeight()) {

				if (child instanceof com.google.android.material.bottomnavigation.BottomNavigationView) {
					return findViewAt((ViewGroup) child, x, y);
				}

				if (child instanceof ViewGroup) {
					View found = findViewAt((ViewGroup) child, x, y);
					if (found != null) return found;
				}
				return child;
			}
		}
		return null;
	}
	
	private void speak(String text) {
		if (p.getBoolean("VOICE_ENABLED", false) && tts != null) {
			try {
				tts.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "TTS_ID");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void setTtsLanguage(String langCode) {
		if (tts == null) return;

		Locale locale;
		switch (langCode) {
			case "ru": locale = new Locale("ru", "RU"); break;
			case "en": locale = Locale.US; break;
			case "kz": locale = new Locale("kk", "KZ"); break;
			default: locale = Locale.getDefault();
		}
		tts.setLanguage(locale);
	}
	
    private String loadJSON() {
        try {
            InputStream is = getAssets().open("data.json");
            byte[] buf = new byte[is.available()];
            is.read(buf); is.close();
            return new String(buf, StandardCharsets.UTF_8);
        } catch (Exception e) { return "{}"; }
    }
	
	private int dpToPx(int dp) {
		float density = getResources().getDisplayMetrics().density;
		return Math.round((float) dp * density);
	}
}

