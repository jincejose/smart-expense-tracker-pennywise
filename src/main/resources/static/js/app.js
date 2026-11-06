(() => {
  const locale = 'en-IN';
  const currency = 'INR';

  function formatMoney(node) {
    const value = Number(node.dataset.amount);
    if (!Number.isFinite(value)) return;
    const digits = node.dataset.digits === undefined ? 2 : Number(node.dataset.digits);
    node.textContent = new Intl.NumberFormat(locale, {
      style: 'currency',
      currency,
      minimumFractionDigits: digits,
      maximumFractionDigits: digits,
    }).format(value);
  }

  document.addEventListener('DOMContentLoaded', () => {
    document.documentElement.lang = 'en-IN';
    document.querySelectorAll('[data-money][data-amount]').forEach(formatMoney);

    document.addEventListener('click', (event) => {
      const target = event.target.closest('[data-scroll-to]');
      if (target) document.getElementById(target.dataset.scrollTo)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });

    document.querySelectorAll('form[data-confirm]').forEach((form) => form.addEventListener('submit', (event) => {
      if (!window.confirm(form.dataset.confirm)) event.preventDefault();
    }));

    document.querySelectorAll('.flash').forEach((flash) => setTimeout(() => flash.classList.add('is-fading'), 4200));
  });
})();
