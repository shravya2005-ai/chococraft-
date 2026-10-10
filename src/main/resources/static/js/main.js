/**
 * ChocoCraft Interactive Frontend JavaScript
 */

document.addEventListener('DOMContentLoaded', function () {
    // 1. Smart Name-Based Image Fallback Handling (Pure Chocolate Local Assets)
    const fallbackMap = {
        'dark': '/images/products/classic-dark-chocolate.jpg',
        'milk': '/images/products/milk-chocolate-bites.jpg',
        'truffle': '/images/products/homemade-chocolate-truffles.jpg',
        'almond': '/images/products/almond-crunch-chocolate.jpg',
        'hazelnut': '/images/products/hazelnut-chocolate.jpg',
        'white': '/images/products/white-chocolate-hearts.jpg',
        'gift': '/images/products/assorted-chocolate-gift-box.jpg',
        'hamper': '/images/products/festive-chocolate-hamper.jpg',
        'caramel': '/images/products/caramel-silk-delight.jpg',
        'berry': '/images/products/berry-fusion-dark-bar.jpg'
    };

    document.querySelectorAll('img').forEach(function (img) {
        img.addEventListener('error', function () {
            if (!this.dataset.fallbackApplied) {
                this.dataset.fallbackApplied = 'true';
                const altText = (this.alt || '').toLowerCase();
                let chosenUrl = '/images/placeholder-chocolate.jpg';
                for (const [key, url] of Object.entries(fallbackMap)) {
                    if (altText.includes(key)) {
                        chosenUrl = url;
                        break;
                    }
                }
                this.src = chosenUrl;
            }
        });
    });

    // 2. Custom Gift Box Builder Logic
    const giftBoxContainer = document.getElementById('giftBoxBuilderContainer');
    if (giftBoxContainer) {
        initGiftBoxBuilder();
    }

    // 3. Mock Online Payment Modal Trigger
    const paymentMethodSelect = document.getElementById('paymentMethodSelect');
    if (paymentMethodSelect) {
        paymentMethodSelect.addEventListener('change', function () {
            const mockAlert = document.getElementById('mockPaymentNotice');
            if (mockAlert) {
                if (this.value === 'MOCK_ONLINE') {
                    mockAlert.classList.remove('d-none');
                } else {
                    mockAlert.classList.add('d-none');
                }
            }
        });
    }
});

function initGiftBoxBuilder() {
    let selectedCapacity = 4;
    let selectedBoxPrice = 150;
    const boxSizeRadios = document.querySelectorAll('input[name="boxSize"]');
    const chocolateQtyInputs = document.querySelectorAll('.choco-qty-input');
    const totalSelectedSpan = document.getElementById('totalSelectedCount');
    const requiredCapacitySpan = document.getElementById('requiredCapacity');
    const dynamicPriceSpan = document.getElementById('dynamicBoxPrice');
    const capacityProgressBar = document.getElementById('capacityProgressBar');

    function updateGiftBoxTotals() {
        let currentTotalCount = 0;
        let chocolatesCost = 0;

        chocolateQtyInputs.forEach(input => {
            const qty = parseInt(input.value) || 0;
            const price = parseFloat(input.dataset.price) || 0;
            currentTotalCount += qty;
            chocolatesCost += (qty * price);
        });

        if (totalSelectedSpan) totalSelectedSpan.textContent = currentTotalCount;
        if (requiredCapacitySpan) requiredCapacitySpan.textContent = selectedCapacity;
        
        const grandTotal = selectedBoxPrice + chocolatesCost;
        if (dynamicPriceSpan) dynamicPriceSpan.textContent = '₹' + grandTotal.toFixed(2);

        if (capacityProgressBar) {
            const percentage = Math.min(100, Math.round((currentTotalCount / selectedCapacity) * 100));
            capacityProgressBar.style.width = percentage + '%';
            capacityProgressBar.textContent = currentTotalCount + ' / ' + selectedCapacity;
            
            if (currentTotalCount === selectedCapacity) {
                capacityProgressBar.className = 'progress-bar bg-success';
            } else if (currentTotalCount > selectedCapacity) {
                capacityProgressBar.className = 'progress-bar bg-danger';
            } else {
                capacityProgressBar.className = 'progress-bar bg-warning text-dark';
            }
        }
    }

    boxSizeRadios.forEach(radio => {
        radio.addEventListener('change', function () {
            selectedCapacity = parseInt(this.dataset.capacity);
            selectedBoxPrice = parseFloat(this.dataset.price);
            document.querySelectorAll('.giftbox-card').forEach(card => card.classList.remove('selected'));
            this.closest('.giftbox-card').classList.add('selected');
            updateGiftBoxTotals();
        });
    });

    document.querySelectorAll('.btn-qty-minus').forEach(btn => {
        btn.addEventListener('click', function () {
            const input = this.nextElementSibling;
            let val = parseInt(input.value) || 0;
            if (val > 0) {
                input.value = val - 1;
                updateGiftBoxTotals();
            }
        });
    });

    document.querySelectorAll('.btn-qty-plus').forEach(btn => {
        btn.addEventListener('click', function () {
            const input = this.previousElementSibling;
            let val = parseInt(input.value) || 0;
            input.value = val + 1;
            updateGiftBoxTotals();
        });
    });

    updateGiftBoxTotals();
}
