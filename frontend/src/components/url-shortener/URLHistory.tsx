import { Copy, ExternalLink, QrCode, Trash2 } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  useDeleteUrlMutation,
  useUrlHistoryQuery,
} from "@/hooks/useUrlShortener";

import { Button } from "@/components/ui/button";
import { QRCode } from "@/components/ui/qr-code";
import SearchBar from "@/components/ui/search";
import type { UrlDTO } from "@/api/schemas";
import { toast } from "sonner";
import { useDebounce } from "@/hooks/useDebounce";
import { useState } from "react";

interface UrlData {
  id: number;
  originalUrl: string;
  shortenedUrl: string;
  createdAt: string;
  clicks: number;
  shortCode: string;
}

export default function URLHistory() {
  // const router = useRouter()
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedUrl, setSelectedUrl] = useState<UrlData | null>(null);
  const [showQrDialog, setShowQrDialog] = useState(false);
  const debouncedSearch = useDebounce(searchQuery, 300);

  // Use React Query to fetch URL history
  const { data: apiUrls, isLoading } = useUrlHistoryQuery();
  const { deleteUrl } = useDeleteUrlMutation();

  // Map API data to local UrlData type
  const urls: UrlData[] = (apiUrls || []).map((url: UrlDTO) => ({
    id: url.id || 0,
    originalUrl: url.originalUrl || "",
    shortenedUrl: `${window.location.origin}/${url.shortCode}`,
    createdAt: url.createdAt || "",
    clicks: url.clicks || 0,
    shortCode: url.shortCode || "",
  }));

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
    toast("Copied to clipboard");
  };

  const handleDelete = (shortCode: string) => {
    deleteUrl(shortCode);
  };

  const showQrCode = (url: UrlData) => {
    setSelectedUrl(url);
    setShowQrDialog(true);
  };

  const filteredUrls = urls.filter(
    (url) =>
      url.originalUrl.toLowerCase().includes(debouncedSearch.toLowerCase()) ||
      url.shortenedUrl.toLowerCase().includes(debouncedSearch.toLowerCase())
  );

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-10">
        <div className="text-center">
          <div className="animate-spin h-8 w-8 border-4 border-primary border-t-transparent rounded-full mx-auto" />
          <p className="mt-4 text-muted-foreground">Loading your URLs...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <SearchBar value={searchQuery} onChange={setSearchQuery} />

      {filteredUrls.length === 0 ? (
        <div className="rounded-lg border border-dashed p-10 text-center">
          <h3 className="text-lg font-medium">No URLs found</h3>
          <p className="mt-2 text-sm text-muted-foreground">
            {urls.length === 0
              ? "You haven't created any shortened URLs yet."
              : "No URLs match your search query."}
          </p>
        </div>
      ) : (
        <div className="rounded-md border px-2">
          <Table>
            <TableHeader>
              <TableRow className="font-xl">
                <TableHead>Original URL</TableHead>
                <TableHead>Short URL</TableHead>
                <TableHead>Created</TableHead>
                <TableHead>Clicks</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredUrls.map((url) => (
                <TableRow key={url.id}>
                  <TableCell
                    className="max-w-[200px] truncate font-mono"
                    title={url.originalUrl}
                  >
                    {url.originalUrl}
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center space-x-2 font-mono">
                      <span className="font-medium">
                        {url.shortenedUrl.split("/").pop()}
                      </span>
                      <Button
                        size="icon"
                        variant="ghost"
                        className="h-8 w-8"
                        onClick={() => copyToClipboard(url.shortenedUrl)}
                        title="Copy to clipboard"
                      >
                        <Copy className="h-4 w-4" />
                      </Button>
                    </div>
                  </TableCell>
                  <TableCell>{formatDate(url.createdAt)}</TableCell>
                  <TableCell>{url.clicks}</TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end space-x-1">
                      <Button
                        size="icon"
                        variant="ghost"
                        className="h-8 w-8"
                        onClick={() => showQrCode(url)}
                        title="Show QR code"
                      >
                        <QrCode className="h-4 w-4" />
                      </Button>

                      <Button
                        size="icon"
                        variant="ghost"
                        className="h-8 w-8"
                        onClick={() => window.open(url.shortenedUrl, "_blank")}
                        title="Open URL"
                      >
                        <ExternalLink className="h-4 w-4" />
                      </Button>
                      <Button
                        size="icon"
                        variant="ghost"
                        className="h-8 w-8 text-red-500 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-950"
                        onClick={() => handleDelete(url.shortCode)}
                        title="Delete URL"
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      <Dialog open={showQrDialog} onOpenChange={setShowQrDialog}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>QR Code</DialogTitle>
            <DialogDescription>
              Scan this QR code to access your shortened URL.
            </DialogDescription>
          </DialogHeader>
          <div className="flex flex-col justify-center py-4 items-center space-y-4">
            {selectedUrl && (
              <QRCode value={selectedUrl.shortenedUrl} size={250} />
            )}
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
