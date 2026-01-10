<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useInvoices } from "../composables/useInvoices";
import { Button } from "@/components/ui/button";
import {
  FileText,
  ArrowLeft,
  Save,
  Plus,
  Trash2,
  Calendar,
} from "lucide-vue-next";
import type { InvoiceItem } from "../types/invoices.types";

const route = useRoute();
const router = useRouter();
const {
  getInvoiceById,
  updateInvoice,
  calculateItemAmount,
  calculateTotals,
  formatCurrency,
} = useInvoices();

const isSubmitting = ref(false);
const invoiceId = route.params.id as string;

const items = ref<InvoiceItem[]>([]);
const formData = ref({
  type: "sale" as string,
  status: "draft" as string,
  customerId: "",
  customerName: "",
  customerEmail: "",
  customerAddress: {
    street: "",
    city: "",
    postalCode: "",
    country: "France",
  },
  date: "" as string,
  dueDate: "" as string,
  currency: "EUR",
  notes: "",
});

const totals = computed(() => {
  return calculateTotals(items.value);
});

onMounted(async () => {
  const invoice = getInvoiceById(invoiceId);
  if (invoice) {
    items.value = [...invoice.items];
    formData.value = {
      type: invoice.type,
      status: invoice.status,
      customerId: invoice.customerId,
      customerName: invoice.customerName,
      customerEmail: invoice.customerEmail || "",
      customerAddress: invoice.customerAddress || {
        street: "",
        city: "",
        postalCode: "",
        country: "France",
      },
      date: invoice.date?.toISOString().split("T")[0] ?? "",
      dueDate: invoice.dueDate?.toISOString().split("T")[0] ?? "",
      currency: invoice.currency,
      notes: invoice.notes || "",
    };
  }
});

function addItem() {
  items.value.push({
    id: `temp-${Date.now()}`,
    productName: "",
    quantity: 1,
    unitPrice: 0,
    taxRate: 20,
    discount: 0,
    amount: 0,
    accountId: "707000",
  });
}

function removeItem(index: number) {
  if (items.value.length > 1) {
    items.value.splice(index, 1);
  }
}

function updateItem(index: number, field: keyof InvoiceItem, value: any) {
  const item = items.value[index];
  if (item) {
    items.value[index] = {
      ...item,
      [field]: value,
      amount: calculateItemAmount(
        field === "quantity" ? Number(value) : item.quantity,
        field === "unitPrice" ? Number(value) : item.unitPrice,
        field === "discount" ? Number(value) : item.discount,
      ),
    };
  }
}

async function handleSubmit() {
  try {
    isSubmitting.value = true;

    const updated = await updateInvoice(invoiceId, {
      customerId: formData.value.customerId || "temp-cust",
      customerName: formData.value.customerName,
      customerEmail: formData.value.customerEmail,
      customerAddress: formData.value.customerAddress,
      date: new Date(formData.value.date),
      dueDate: new Date(formData.value.dueDate),
      items: items.value,
      subtotal: totals.value.subtotal,
      taxTotal: totals.value.taxTotal,
      total: totals.value.total,
      notes: formData.value.notes,
    });

    if (updated) {
      router.push({ name: "invoices-detail", params: { id: invoiceId } });
    }
  } catch (error) {
    console.error("Error updating invoice:", error);
  } finally {
    isSubmitting.value = false;
  }
}

function handleCancel() {
  router.push({ name: "invoices-detail", params: { id: invoiceId } });
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-4">
        <Button variant="ghost" size="icon" @click="handleCancel">
          <ArrowLeft class="h-4 w-4" />
        </Button>
        <div>
          <h1 class="text-2xl font-bold flex items-center gap-2">
            <FileText class="h-6 w-6" />
            Modifier facture
          </h1>
          <p class="text-muted-foreground">
            Modifier les détails de la facture
          </p>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <Button variant="outline" @click="handleCancel"> Annuler </Button>
        <Button @click="handleSubmit" :disabled="isSubmitting">
          <Save class="h-4 w-4 mr-2" />
          Enregistrer
        </Button>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Main Form -->
      <div class="lg:col-span-2 space-y-6">
        <div class="rounded-xl border bg-card p-6">
          <h2 class="text-lg font-semibold mb-4">Informations client</h2>

          <div class="grid grid-cols-2 gap-4">
            <div class="col-span-2">
              <label class="block text-sm font-medium mb-2"
                >Nom du client</label
              >
              <input
                v-model="formData.customerName"
                type="text"
                class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
                placeholder="Entreprise XYZ"
              />
            </div>
            <div class="col-span-2">
              <label class="block text-sm font-medium mb-2">Email</label>
              <input
                v-model="formData.customerEmail"
                type="email"
                class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
                placeholder="contact@entreprise.com"
              />
            </div>
            <div class="col-span-2">
              <label class="block text-sm font-medium mb-2">Adresse</label>
              <input
                v-model="formData.customerAddress.street"
                type="text"
                class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
                placeholder="123 Rue du Commerce"
              />
            </div>
            <div>
              <label class="block text-sm font-medium mb-2">Code postal</label>
              <input
                v-model="formData.customerAddress.postalCode"
                type="text"
                class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
                placeholder="75001"
              />
            </div>
            <div>
              <label class="block text-sm font-medium mb-2">Ville</label>
              <input
                v-model="formData.customerAddress.city"
                type="text"
                class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
                placeholder="Paris"
              />
            </div>
          </div>
        </div>

        <div class="rounded-xl border bg-card p-6">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold">Articles</h2>
            <Button variant="outline" size="sm" @click="addItem">
              <Plus class="h-4 w-4 mr-2" />
              Ajouter
            </Button>
          </div>

          <div class="space-y-3">
            <div
              v-for="(item, index) in items"
              :key="item.id"
              class="rounded-lg border p-4 space-y-3"
            >
              <div class="grid grid-cols-12 gap-3">
                <div class="col-span-5">
                  <label class="block text-xs font-medium mb-1">Produit</label>
                  <input
                    :value="item.productName"
                    @input="
                      updateItem(
                        index,
                        'productName',
                        ($event.target as HTMLInputElement).value,
                      )
                    "
                    type="text"
                    class="w-full px-2 py-1.5 rounded border bg-background text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    placeholder="Nom du produit"
                  />
                </div>
                <div class="col-span-2">
                  <label class="block text-xs font-medium mb-1">Qté</label>
                  <input
                    :value="item.quantity"
                    @input="
                      updateItem(
                        index,
                        'quantity',
                        Number(($event.target as HTMLInputElement).value),
                      )
                    "
                    type="number"
                    min="1"
                    class="w-full px-2 py-1.5 rounded border bg-background text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                  />
                </div>
                <div class="col-span-2">
                  <label class="block text-xs font-medium mb-1"
                    >Prix unit.</label
                  >
                  <input
                    :value="item.unitPrice"
                    @input="
                      updateItem(
                        index,
                        'unitPrice',
                        Number(($event.target as HTMLInputElement).value),
                      )
                    "
                    type="number"
                    min="0"
                    step="0.01"
                    class="w-full px-2 py-1.5 rounded border bg-background text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                  />
                </div>
                <div class="col-span-2">
                  <label class="block text-xs font-medium mb-1">TVA %</label>
                  <input
                    :value="item.taxRate"
                    @input="
                      updateItem(
                        index,
                        'taxRate',
                        Number(($event.target as HTMLInputElement).value),
                      )
                    "
                    type="number"
                    min="0"
                    class="w-full px-2 py-1.5 rounded border bg-background text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                  />
                </div>
                <div class="col-span-1 flex items-end">
                  <Button
                    variant="ghost"
                    size="icon-sm"
                    class="text-destructive hover:text-destructive"
                    @click="removeItem(index)"
                  >
                    <Trash2 class="h-4 w-4" />
                  </Button>
                </div>
              </div>
              <div
                class="flex items-center justify-between text-sm pt-2 border-t"
              >
                <span class="text-muted-foreground">Total ligne</span>
                <span class="font-medium">{{
                  formatCurrency(item.amount)
                }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Sidebar -->
      <div class="space-y-6">
        <div class="rounded-xl border bg-card p-6">
          <h2 class="text-lg font-semibold mb-4 flex items-center gap-2">
            <Calendar class="h-5 w-5" />
            Dates
          </h2>
          <div class="space-y-3">
            <div>
              <label class="block text-sm font-medium mb-2"
                >Date de facturation</label
              >
              <input
                v-model="formData.date"
                type="date"
                class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
              />
            </div>
            <div>
              <label class="block text-sm font-medium mb-2"
                >Date d'échéance</label
              >
              <input
                v-model="formData.dueDate"
                type="date"
                class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
              />
            </div>
          </div>
        </div>

        <div class="rounded-xl border bg-card p-6">
          <h2 class="text-lg font-semibold mb-4">Récapitulatif</h2>
          <div class="space-y-3">
            <div class="flex items-center justify-between text-sm">
              <span class="text-muted-foreground">Sous-total</span>
              <span class="font-medium">{{
                formatCurrency(totals.subtotal)
              }}</span>
            </div>
            <div class="flex items-center justify-between text-sm">
              <span class="text-muted-foreground">TVA</span>
              <span class="font-medium">{{
                formatCurrency(totals.taxTotal)
              }}</span>
            </div>
            <div
              class="border-t pt-3 flex items-center justify-between text-lg font-bold"
            >
              <span>Total</span>
              <span>{{ formatCurrency(totals.total) }}</span>
            </div>
          </div>
        </div>

        <div class="rounded-xl border bg-card p-6">
          <label class="block text-sm font-medium mb-2">Notes</label>
          <textarea
            v-model="formData.notes"
            rows="4"
            class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring resize-none"
            placeholder="Notes ou conditions particulières..."
          />
        </div>
      </div>
    </div>
  </div>
</template>
