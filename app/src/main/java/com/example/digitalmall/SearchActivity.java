package com.example.digitalmall;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalmall.adapter.SearchAdapter;
import com.example.digitalmall.model.Product;
import com.example.digitalmall.model.SearchItem;
import com.example.digitalmall.net.Api;
import com.example.digitalmall.net.HttpUtil;
import com.example.digitalmall.net.JsonParser;
import com.example.digitalmall.util.CartManager;
import com.example.digitalmall.util.RawData;
import com.example.digitalmall.util.SearchHistory;
import com.example.digitalmall.util.UIUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 商品搜索页。
 * <p>
 * 数据来源：把 store_detail.json 里 5 家店、15 个分类、150 件商品摊平成一条列表，
 * 用户输入关键词后在本地做「空格分词 + 全部命中」的匹配，命中结果用 ListView 展示。
 * 网络数据是异步拉取的，先读 res/raw 的离线数据保证秒开，服务器返回后再覆盖并重跑当前查询。
 */
public class SearchActivity extends AppCompatActivity implements SearchAdapter.OnSearchActionListener {

    /** 进页面时直接带上关键词（热词、历史、外部跳转都可以用） */
    public static final String EXTRA_KEYWORD = "extra_keyword";

    private EditText etSearch;
    private ImageView ivClear;
    private LinearLayout layoutSuggest;
    private LinearLayout layoutResult;
    private LinearLayout layoutHistory;
    private LinearLayout llHistory;
    private LinearLayout llHot;
    private LinearLayout layoutEmpty;
    private TextView tvResultCount;
    private TextView tvEmptyTitle;
    private ListView lvResult;

    private SearchAdapter adapter;

    /** 全部商品（跨店铺摊平），搜索就是在这份数据上做过滤 */
    private final List<SearchItem> allItems = new ArrayList<>();
    /** 当前生效的关键词，服务器数据回来时用它重跑一次，保证结果不丢 */
    private String currentKeyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        loadData();

        String preset = getIntent().getStringExtra(EXTRA_KEYWORD);
        if (!TextUtils.isEmpty(preset)) {
            etSearch.setText(preset);
            etSearch.setSelection(preset.length());
            doSearch(preset);
        } else {
            showKeyboard();
        }
    }

    private void initViews() {
        etSearch = findViewById(R.id.et_search);
        ivClear = findViewById(R.id.iv_clear);
        layoutSuggest = findViewById(R.id.layout_suggest);
        layoutResult = findViewById(R.id.layout_result);
        layoutHistory = findViewById(R.id.layout_history);
        llHistory = findViewById(R.id.ll_history);
        llHot = findViewById(R.id.ll_hot);
        layoutEmpty = findViewById(R.id.layout_empty);
        tvResultCount = findViewById(R.id.tv_result_count);
        tvEmptyTitle = findViewById(R.id.tv_empty_title);
        lvResult = findViewById(R.id.lv_result);

        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.tv_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSearch(etSearch.getText().toString());
            }
        });

        findViewById(R.id.tv_clear_history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchHistory.clear(SearchActivity.this);
                renderHistory();
            }
        });

        ivClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearch.setText("");
                etSearch.requestFocus();
                showKeyboard();
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean hasText = s != null && s.length() > 0;
                ivClear.setVisibility(hasText ? View.VISIBLE : View.GONE);
                // 输入被清空时退回到「历史 + 热词」状态
                if (!hasText) {
                    showSuggest();
                }
            }
        });

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    doSearch(etSearch.getText().toString());
                    return true;
                }
                return false;
            }
        });

        adapter = new SearchAdapter(this);
        adapter.setOnSearchActionListener(this);
        lvResult.setAdapter(adapter);

        fillHotWords();
        renderHistory();
    }

    /** 先用离线数据填充，再异步拉取服务器数据覆盖 */
    private void loadData() {
        try {
            allItems.addAll(JsonParser.parseAllProducts(RawData.storeDetailJson(this)));
        } catch (Exception ignored) {
            // 离线数据异常时不阻塞页面，等服务器数据
        }

        Api.getStoreDetail(new HttpUtil.Callback() {
            @Override
            public void onSuccess(String body) {
                try {
                    List<SearchItem> items = JsonParser.parseAllProducts(body);
                    if (items.isEmpty()) {
                        return;
                    }
                    allItems.clear();
                    allItems.addAll(items);
                    if (!currentKeyword.isEmpty()) {
                        doSearch(currentKeyword);
                    }
                } catch (Exception ignored) {
                    // 解析失败就继续用离线数据
                }
            }

            @Override
            public void onFailure(String message) {
                // 离线数据已经可用，这里不打扰用户
            }
        });
    }

    private void doSearch(String rawKeyword) {
        String keyword = rawKeyword == null ? "" : rawKeyword.trim();
        if (keyword.isEmpty()) {
            UIUtil.toast(this, getString(R.string.search_need_keyword));
            return;
        }

        currentKeyword = keyword;
        SearchHistory.add(this, keyword);
        renderHistory();

        List<SearchItem> result = filter(keyword);
        adapter.setData(result, keyword);

        layoutSuggest.setVisibility(View.GONE);
        layoutResult.setVisibility(View.VISIBLE);
        tvResultCount.setText(result.isEmpty()
                ? "" : getString(R.string.search_result_count, result.size()));
        layoutEmpty.setVisibility(result.isEmpty() ? View.VISIBLE : View.GONE);
        tvEmptyTitle.setText(getString(R.string.search_empty_title, keyword));
        lvResult.setSelection(0);

        hideKeyboard();

        lvResult.setAlpha(0f);
        lvResult.animate().alpha(1f).setDuration(240).start();
    }

    /** 空格分词，每个词都要命中（商品名 / 副标题 / 描述 / 参数 / 店铺名 / 分类名） */
    private List<SearchItem> filter(String keyword) {
        List<SearchItem> result = new ArrayList<>();
        String[] tokens = keyword.toLowerCase(Locale.ROOT).split("\\s+");
        for (SearchItem item : allItems) {
            String text = item.searchableText();
            boolean hit = true;
            for (String token : tokens) {
                if (token.isEmpty()) {
                    continue;
                }
                if (!text.contains(token)) {
                    hit = false;
                    break;
                }
            }
            if (hit) {
                result.add(item);
            }
        }
        return result;
    }

    private void showSuggest() {
        layoutResult.setVisibility(View.GONE);
        layoutSuggest.setVisibility(View.VISIBLE);
        adapter.setData(null, null);
    }

    private void renderHistory() {
        List<String> history = SearchHistory.get(this);
        layoutHistory.setVisibility(history.isEmpty() ? View.GONE : View.VISIBLE);
        fillTags(llHistory, history);
    }

    private void fillHotWords() {
        fillTags(llHot, java.util.Arrays.asList(getResources().getStringArray(R.array.search_hot_words)));
    }

    /**
     * 关键词按可用宽度自动换行。
     * 手机屏只有 360dp 宽，一行放不下全部热词，靠横向滚动会让最后一个词只露出半截，
     * 所以这里量一下每个胶囊的宽度，放不下就另起一行。
     */
    private void fillTags(LinearLayout container, List<String> words) {
        container.removeAllViews();
        if (words == null || words.isEmpty()) {
            return;
        }
        int avail = getResources().getDisplayMetrics().widthPixels
                - UIUtil.dp(this, 28) - UIUtil.dp(this, 2);
        LinearLayout row = null;
        int used = 0;
        for (String word : words) {
            View tag = createTag(word);
            tag.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            int w = tag.getMeasuredWidth() + UIUtil.dp(this, 8);
            if (row == null || used + w > avail) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                rowParams.topMargin = UIUtil.dp(this, 8);
                container.addView(row, rowParams);
                used = 0;
            }
            row.addView(tag);
            used += w;
        }
    }

    /** 生成一个可点击的关键词胶囊 */
    private View createTag(final String word) {
        TextView tag = new TextView(this);
        tag.setText(word);
        tag.setTextSize(12);
        tag.setTextColor(getColor(R.color.text_secondary));
        tag.setBackgroundResource(R.drawable.bg_keyword);
        tag.setPadding(UIUtil.dp(this, 12), UIUtil.dp(this, 6),
                UIUtil.dp(this, 12), UIUtil.dp(this, 6));
        tag.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.rightMargin = UIUtil.dp(this, 8);
        tag.setLayoutParams(params);
        tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearch.setText(word);
                etSearch.setSelection(word.length());
                doSearch(word);
            }
        });
        return tag;
    }

    private void showKeyboard() {
        etSearch.requestFocus();
        etSearch.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }, 200);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
        etSearch.clearFocus();
    }

    @Override
    public void onItemClick(SearchItem item) {
        Product product = item.getProduct();
        if (product == null) {
            return;
        }
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT, product);
        intent.putExtra(ProductDetailActivity.EXTRA_STORE_ID, item.getStoreId());
        intent.putExtra(ProductDetailActivity.EXTRA_THEME_COLOR, item.getThemeColor());
        startActivity(intent);
    }

    @Override
    public void onAddToCart(SearchItem item) {
        Product product = item.getProduct();
        if (product == null) {
            return;
        }
        CartManager.getInstance().add(item.getStoreId(), product);
        adapter.notifyDataSetChanged();
        UIUtil.toast(this, getString(R.string.added_to_cart));
    }
}
