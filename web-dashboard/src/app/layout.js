import "./globals.css";

export const metadata = {
  title: "AMIHA ICS Manager - Dashboard",
  description: "Web Dashboard for organic farming certification, staff management, and analytics.",
};

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <head>
        <link rel="icon" href="/favicon.ico" />
      </head>
      <body>{children}</body>
    </html>
  );
}
