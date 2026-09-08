// Enviro365 Withdrawal Notices — frontend logic
// Talks to the Spring Boot API. Change API_BASE if the backend runs elsewhere.
const API_BASE = "http://localhost:8080/api";

const state = {
  investors: [],
  currentInvestorId: null,
  portfolio: null,
};

const el = {
  investorTabs: document.getElementById("investorTabs"),
  totalBalance: document.getElementById("totalBalance"),
  investorLine: document.getElementById("investorLine"),
  openFormBtn: document.getElementById("openFormBtn"),
  cancelFormBtn: document.getElementById("cancelFormBtn"),
  withdrawForm: document.getElementById("withdrawForm"),
  productSelect: document.getElementById("productSelect"),
  amountInput: document.getElementById("amountInput"),
  limitHint: document.getElementById("limitHint"),
  holdingsBody: document.getElementById("holdingsBody"),
  historyBody: document.getElementById("historyBody"),
  filterProduct: document.getElementById("filterProduct"),
  filterFrom: document.getElementById("filterFrom"),
  filterTo: document.getElementById("filterTo"),
  exportBtn: document.getElementById("exportBtn"),
  toast: document.getElementById("toast"),
};

const zar = new Intl.NumberFormat("en-ZA", { style: "currency", currency: "ZAR" });
const dateFmt = new Intl.DateTimeFormat("en-ZA", { year: "numeric", month: "short", day: "2-digit", hour: "2-digit", minute: "2-digit" });

init();

async function init() {
  bindEvents();
  await loadInvestors();
}

function bindEvents() {
  el.openFormBtn.addEventListener("click", () => {
    el.withdrawForm.hidden = false;
    el.openFormBtn.hidden = true;
    el.productSelect.focus();
  });

  el.cancelFormBtn.addEventListener("click", () => resetForm());

  el.productSelect.addEventListener("change", updateLimitHint);
  el.amountInput.addEventListener("input", updateLimitHint);

  el.withdrawForm.addEventListener("submit", handleSubmitWithdrawal);

  el.exportBtn.addEventListener("click", handleExport);
}

async function loadInvestors() {
  try {
    const investors = await getJSON(`${API_BASE}/investors`);
    state.investors = investors;
    renderInvestorTabs();
    if (investors.length > 0) {
      await selectInvestor(investors[0].id);
    }
  } catch (err) {
    showToast("Could not reach the API. Is the backend running on port 8080?", true);
  }
}

function renderInvestorTabs() {
  el.investorTabs.innerHTML = "";
  state.investors.forEach((inv) => {
    const btn = document.createElement("button");
    btn.className = "investor-tab";
    btn.type = "button";
    btn.setAttribute("role", "tab");
    btn.setAttribute("aria-selected", String(inv.id === state.currentInvestorId));
    btn.innerHTML = `${escapeHtml(inv.fullName)}<span class="age">age ${inv.age}</span>`;
    btn.addEventListener("click", () => selectInvestor(inv.id));
    el.investorTabs.appendChild(btn);
  });
}

async function selectInvestor(investorId) {
  state.currentInvestorId = investorId;
  renderInvestorTabs();
  resetForm();
  await Promise.all([loadPortfolio(investorId), loadHistory(investorId)]);
}

async function loadPortfolio(investorId) {
  try {
    const portfolio = await getJSON(`${API_BASE}/investors/${investorId}/portfolio`);
    state.portfolio = portfolio;
    renderStub(portfolio);
    renderHoldings(portfolio);
    renderProductOptions(portfolio);
  } catch (err) {
    showToast("Could not load that investor's portfolio.", true);
  }
}

function renderStub(portfolio) {
  el.totalBalance.textContent = zar.format(portfolio.totalBalance);
  el.investorLine.textContent = `${portfolio.fullName} · age ${portfolio.age} · ${portfolio.products.length} product${portfolio.products.length === 1 ? "" : "s"}`;
}

function renderHoldings(portfolio) {
  el.holdingsBody.innerHTML = "";
  portfolio.products.forEach((p) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${escapeHtml(p.productName)}</td>
      <td class="product-type">${formatProductType(p.productType)}</td>
      <td class="num">${zar.format(p.balance)}</td>
      <td class="num">${zar.format(p.maxWithdrawable)}</td>
    `;
    el.holdingsBody.appendChild(tr);
  });
}

function renderProductOptions(portfolio) {
  const options = portfolio.products
    .map((p) => `<option value="${p.id}">${escapeHtml(p.productName)}</option>`)
    .join("");
  el.productSelect.innerHTML = options;

  el.filterProduct.innerHTML =
    `<option value="">All products</option>` +
    portfolio.products.map((p) => `<option value="${p.id}">${escapeHtml(p.productName)}</option>`).join("");

  updateLimitHint();
}

function updateLimitHint() {
  const productId = Number(el.productSelect.value);
  const product = state.portfolio?.products.find((p) => p.id === productId);
  if (!product) {
    el.limitHint.textContent = "";
    return;
  }

  const amount = Number(el.amountInput.value || 0);
  let message = `Up to ${zar.format(product.maxWithdrawable)} available (90% of ${zar.format(product.balance)} balance).`;
  let warning = false;

  if (product.retirementProduct && state.portfolio.age <= 65) {
    message = "Retirement product: withdrawals require the investor to be older than 65.";
    warning = true;
  } else if (amount > product.balance) {
    message = "Amount exceeds this product's balance.";
    warning = true;
  } else if (amount > product.maxWithdrawable) {
    message = "Amount exceeds the 90% withdrawal limit for this product.";
    warning = true;
  }

  el.limitHint.textContent = message;
  el.limitHint.classList.toggle("is-warning", warning);
}

async function handleSubmitWithdrawal(event) {
  event.preventDefault();
  const submitBtn = event.submitter;
  submitBtn.disabled = true;

  const payload = {
    investorId: state.currentInvestorId,
    productId: Number(el.productSelect.value),
    amount: Number(el.amountInput.value),
  };

  try {
    await postJSON(`${API_BASE}/withdrawals`, payload);
    showToast("Withdrawal notice submitted.");
    resetForm();
    await Promise.all([loadPortfolio(state.currentInvestorId), loadHistory(state.currentInvestorId)]);
  } catch (err) {
    showToast(err.message || "The withdrawal could not be processed.", true);
  } finally {
    submitBtn.disabled = false;
  }
}

function resetForm() {
  el.withdrawForm.reset();
  el.withdrawForm.hidden = true;
  el.openFormBtn.hidden = false;
  el.limitHint.textContent = "";
}

async function loadHistory(investorId) {
  try {
    const params = new URLSearchParams();
    if (el.filterProduct.value) params.set("productId", el.filterProduct.value);
    const notices = await getJSON(`${API_BASE}/withdrawals/investor/${investorId}?${params.toString()}`);
    renderHistory(notices);
  } catch (err) {
    showToast("Could not load withdrawal history.", true);
  }
}

el.filterProduct.addEventListener("change", () => loadHistory(state.currentInvestorId));

function renderHistory(notices) {
  if (notices.length === 0) {
    el.historyBody.innerHTML = `<tr class="empty-row"><td colspan="5">No withdrawal notices yet.</td></tr>`;
    return;
  }

  el.historyBody.innerHTML = notices
    .map((n, index) => `
      <tr class="${index === 0 ? "new-notice-row" : ""}">
        <td>${dateFmt.format(new Date(n.requestedAt))}</td>
        <td>${escapeHtml(n.productName)}</td>
        <td class="num">${zar.format(n.amount)}</td>
        <td class="num">${zar.format(n.balanceAfter)}</td>
        <td><span class="status-pill status-pill--${n.status.toLowerCase()}">${n.status}</span></td>
      </tr>
    `)
    .join("");
}

function handleExport() {
  if (!state.currentInvestorId) return;
  const params = new URLSearchParams({ investorId: state.currentInvestorId });
  if (el.filterProduct.value) params.set("productId", el.filterProduct.value);
  if (el.filterFrom.value) params.set("startDate", el.filterFrom.value);
  if (el.filterTo.value) params.set("endDate", el.filterTo.value);

  window.open(`${API_BASE}/withdrawals/export?${params.toString()}`, "_blank");
}

// ---------- helpers ----------

async function getJSON(url) {
  const res = await fetch(url);
  if (!res.ok) throw await toError(res);
  return res.json();
}

async function postJSON(url, body) {
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!res.ok) throw await toError(res);
  return res.json();
}

async function toError(res) {
  try {
    const data = await res.json();
    const message = Array.isArray(data.messages) ? data.messages.join(" ") : data.message;
    return new Error(message || `Request failed (${res.status})`);
  } catch {
    return new Error(`Request failed (${res.status})`);
  }
}

function formatProductType(type) {
  return type.replace(/_/g, " ").toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase());
}

function escapeHtml(str) {
  const div = document.createElement("div");
  div.textContent = str;
  return div.innerHTML;
}

let toastTimer;
function showToast(message, isError = false) {
  clearTimeout(toastTimer);
  el.toast.textContent = message;
  el.toast.classList.toggle("is-error", isError);
  el.toast.classList.add("is-visible");
  toastTimer = setTimeout(() => el.toast.classList.remove("is-visible"), 4200);
}
