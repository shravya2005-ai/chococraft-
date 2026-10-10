/**
 * ChocoCraft Interactive Frontend JavaScript
 */

document.addEventListener('DOMContentLoaded', function () {
    // 1. Smart Name-Based Image Fallback Handling (Pure Chocolate Only)
    const fallbackMap = {
        'dark': 'https://images.unsplash.com/photo-1548907040-4baa42d10919?auto=format&fit=crop&w=600&q=80',
        'milk': 'https://images.unsplash.com/photo-1541781774459-bb2af2f05b55?auto=format&fit=crop&w=600&q=80',
        'truffle': 'https://images.unsplash.com/photo-1549007994-cb92caebd54b?auto=format&fit=crop&w=600&q=80',
        'almond': 'https://images.unsplash.com/photo-1565071783280-719b01b29912?auto=format&fit=crop&w=600&q=80',
        'hazelnut': 'https://images.unsplash.com/photo-1511381939415-e44015466834?auto=format&fit=crop&w=600&q=80',
        'white': 'https://images.unsplash.com/photo-1581798459219-318e76aecc7b?auto=format&fit=crop&w=600&q=80',
        'gift': 'https://images.unsplash.com/photo-1481391319762-47dff72954d9?auto=format&fit=crop&w=600&q=80',
        'hamper': 'https://images.unsplash.com/photo-1526080652727-5b77f74eacd2?auto=format&fit=crop&w=600&q=80',
        'caramel': 'https://images.unsplash.com/photo-1605807646983-377bc5a76493?auto=format&fit=crop&w=600&q=80',
        'berry': 'https://images.unsplash.com/photo-1623341214825-9f4f963727da?auto=format&fit=crop&w=600&q=80'
    };

    document.querySelectorAll('img').forEach(function (img) {
        img.addEventListener('error', function () {
            if (!this.dataset.fallbackApplied) {
                this.dataset.fallbackApplied = 'true';
                const altText = (this.alt || '').toLowerCase();
                let chosenUrl = 'https://images.unsplash.com/photo-1549007994-cb92caebd54b?auto=format&fit=crop&w=600&q=80';
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
