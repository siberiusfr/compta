<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useInvoices } from '../composables/useInvoices'
import { Button } from '@/components/ui/button'
import {
  FileText,
  ArrowLeft,
  Edit,
  Send,
  Download,
  Printer,
  CheckCircle2,
  X,
  Calendar,
  User,
  Mail,
  MapPin,
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const route = useRoute()
const router = useRouter()
const { getInvoiceById, formatCurrency, formatDate, getInvoiceStatusColor, getInvoiceStatusLabel } =
  useInvoices()

const invoiceId = route.params.id as string
const invoice = getInvoiceById(invoiceId)

function handleEdit() {
  router.push({ name: 'invoices-edit', params: { id: invoiceId } })
}

function handleBack() {
  router.push({ name: 'invoices-list' })
}
</script>

<template>
  <div
    v-if="invoice"
    class="space-y-6"
  >
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-4">
        <Button
          variant="ghost"
          size="icon"
          @click="handleBack"
        >
          <ArrowLeft class="h-4 w-4" />
        </Button>
        <div>
          <h1 class="text-2xl font-bold flex items-center gap-2">
            <FileText class="h-6 w-6" />
            {{ invoice.invoiceNumber }}
          </h1>
          <p class="text-muted-foreground">
            {{ invoice.customerName }}
          </p>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <span :class="cn('text-xs px-3 py-1 rounded-full', getInvoiceStatusColor(invoice.status))">
          {{ getInvoiceStatusLabel(invoice.status) }}
        </span>
        <Button
          variant="outline"
          size="sm"
        >
          <Download class="h-4 w-4 mr-2" />
          Télécharger
        </Button>
        <Button
          variant="outline"
          size="sm"
        >
          <Printer class="h-4 w-4 mr-2" />
          Imprimer
        </Button>
        <Button
          v-if="invoice.status === 'draft'"
          @click="handleEdit"
        >
          <Edit class="h-4 w-4 mr-2" />
          Modifier
        </Button>
      </div>
    </div>

    <!-- Invoice Details -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Main Content -->
      <div class="lg:col-span-2 space-y-6">
        <div class="rounded-xl border bg-card p-6">
          <h2 class="text-lg font-semibold mb-4">Détails de la facture</h2>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <p class="text-sm text-muted-foreground mb-1">Date de facturation</p>
              <p class="font-medium flex items-center gap-2">
                <Calendar class="h-4 w-4" />
                {{ formatDate(invoice.date) }}
              </p>
            </div>
            <div>
              <p class="text-sm text-muted-foreground mb-1">Date d'échéance</p>
              <p class="font-medium flex items-center gap-2">
                <Calendar class="h-4 w-4" />
                {{ formatDate(invoice.dueDate) }}
              </p>
            </div>
          </div>

          <div
            v-if="invoice.notes"
            class="mt-6"
          >
            <p class="text-sm text-muted-foreground mb-2">Notes</p>
            <p class="text-sm">{{ invoice.notes }}</p>
          </div>
        </div>

        <div class="rounded-xl border bg-card p-6">
          <h2 class="text-lg font-semibold mb-4">Articles</h2>
          <table class="w-full">
            <thead class="text-sm text-muted-foreground border-b">
              <tr>
                <th class="text-left py-3">Article</th>
                <th class="text-right py-3">Qté</th>
                <th class="text-right py-3">Prix unitaire</th>
                <th class="text-right py-3">TVA</th>
                <th class="text-right py-3">Total</th>
              </tr>
            </thead>
            <tbody class="divide-y">
              <tr
                v-for="item in invoice.items"
                :key="item.id"
                class="text-sm"
              >
                <td class="py-3">
                  <p class="font-medium">{{ item.productName }}</p>
                  <p
                    v-if="item.description"
                    class="text-muted-foreground text-xs"
                  >
                    {{ item.description }}
                  </p>
                </td>
                <td class="text-right py-3">{{ item.quantity }}</td>
                <td class="text-right py-3">
                  {{ formatCurrency(item.unitPrice) }}
                </td>
                <td class="text-right py-3">{{ item.taxRate }}%</td>
                <td class="text-right py-3 font-medium">
                  {{ formatCurrency(item.amount) }}
                </td>
              </tr>
            </tbody>
            <tfoot class="border-t mt-4">
              <tr>
                <td
                  colspan="4"
                  class="pt-4 text-right text-muted-foreground"
                >
                  Sous-total
                </td>
                <td class="pt-4 text-right font-medium">
                  {{ formatCurrency(invoice.subtotal) }}
                </td>
              </tr>
              <tr v-if="invoice.discountTotal > 0">
                <td
                  colspan="4"
                  class="pt-2 text-right text-muted-foreground"
                >
                  Remise
                </td>
                <td class="pt-2 text-right font-medium text-green-600">
                  -{{ formatCurrency(invoice.discountTotal) }}
                </td>
              </tr>
              <tr>
                <td
                  colspan="4"
                  class="pt-2 text-right text-muted-foreground"
                >
                  TVA
                </td>
                <td class="pt-2 text-right font-medium">
                  {{ formatCurrency(invoice.taxTotal) }}
                </td>
              </tr>
              <tr class="text-lg font-bold">
                <td
                  colspan="4"
                  class="pt-4 text-right"
                >
                  Total
                </td>
                <td class="pt-4 text-right">
                  {{ formatCurrency(invoice.total) }}
                </td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>

      <!-- Sidebar -->
      <div class="space-y-6">
        <div class="rounded-xl border bg-card p-6">
          <h2 class="text-lg font-semibold mb-4 flex items-center gap-2">
            <User class="h-5 w-5" />
            Client
          </h2>
          <div class="space-y-3">
            <div>
              <p class="font-medium">{{ invoice.customerName }}</p>
            </div>
            <div
              v-if="invoice.customerEmail"
              class="flex items-center gap-2 text-sm text-muted-foreground"
            >
              <Mail class="h-4 w-4" />
              {{ invoice.customerEmail }}
            </div>
            <div
              v-if="invoice.customerAddress"
              class="flex items-start gap-2 text-sm text-muted-foreground"
            >
              <MapPin class="h-4 w-4 mt-0.5" />
              <p>
                {{ invoice.customerAddress.street }}<br />
                {{ invoice.customerAddress.postalCode }}
                {{ invoice.customerAddress.city }}<br />
                {{ invoice.customerAddress.country }}
              </p>
            </div>
          </div>
        </div>

        <div class="rounded-xl border bg-card p-6">
          <h2 class="text-lg font-semibold mb-4">Statut</h2>
          <div class="space-y-3">
            <div class="flex items-center justify-between">
              <span class="text-sm text-muted-foreground">Statut actuel</span>
              <span
                :class="cn('text-xs px-2 py-1 rounded-full', getInvoiceStatusColor(invoice.status))"
              >
                {{ getInvoiceStatusLabel(invoice.status) }}
              </span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-sm text-muted-foreground">Créé le</span>
              <span class="text-sm font-medium">{{ formatDate(invoice.createdAt) }}</span>
            </div>
            <div
              v-if="invoice.sentAt"
              class="flex items-center justify-between"
            >
              <span class="text-sm text-muted-foreground">Envoyé le</span>
              <span class="text-sm font-medium">{{ formatDate(invoice.sentAt) }}</span>
            </div>
            <div
              v-if="invoice.paidAt"
              class="flex items-center justify-between"
            >
              <span class="text-sm text-muted-foreground flex items-center gap-1">
                <CheckCircle2 class="h-3 w-3 text-green-600" />
                Payé le
              </span>
              <span class="text-sm font-medium">{{ formatDate(invoice.paidAt) }}</span>
            </div>
          </div>
        </div>

        <div
          v-if="invoice.status === 'draft'"
          class="rounded-xl border bg-card p-6"
        >
          <h2 class="text-lg font-semibold mb-4">Actions</h2>
          <div class="space-y-2">
            <Button
              class="w-full"
              @click="handleEdit"
            >
              <Edit class="h-4 w-4 mr-2" />
              Modifier
            </Button>
            <Button
              variant="outline"
              class="w-full"
            >
              <Send class="h-4 w-4 mr-2" />
              Envoyer
            </Button>
            <Button
              variant="outline"
              class="w-full text-destructive hover:text-destructive"
            >
              <X class="h-4 w-4 mr-2" />
              Annuler
            </Button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div
    v-else
    class="text-center py-12 text-muted-foreground"
  >
    Facture non trouvée
  </div>
</template>
