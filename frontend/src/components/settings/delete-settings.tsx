import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useDeleteAccountMutation } from "@/hooks/useUserSettings";
import { useState } from "react";

export default function DeleteAccountSettings() {
  const deleteMutation = useDeleteAccountMutation();
  const [deleteConfirm, setDeleteConfirm] = useState("");

  return (
    <Card>
      <CardHeader>
        <CardTitle>Delete Account</CardTitle>
        <CardDescription>
          <span className="text-red-600 font-semibold">Warning:</span> This
          action is irreversible. All your data will be permanently deleted.
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <form
          id="delete-form"
          onSubmit={(e) => {
            e.preventDefault();
            deleteMutation.mutate();
          }}
          className="space-y-4"
        >
          <div>
            <label className="block mb-1 font-medium" htmlFor="delete-confirm">
              Type <span className="font-bold">DELETE</span> to confirm:
            </label>
            <Input
              id="delete-confirm"
              value={deleteConfirm}
              onChange={(e) => setDeleteConfirm(e.target.value)}
              placeholder="DELETE"
              autoComplete="off"
              autoCapitalize="on"
            />
          </div>
        </form>
      </CardContent>
      <CardFooter>
        <Button
          type="submit"
          form="delete-form"
          variant="destructive"
          disabled={deleteConfirm !== "DELETE" || deleteMutation.isPending}
        >
          {deleteMutation.isPending ? "Deleting..." : "Delete My Account"}
        </Button>
      </CardFooter>
    </Card>
  );
}
